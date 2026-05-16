package com.example.kabaddiarena.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddiarena.data.local.entity.MatchEventEntity
import com.example.kabaddiarena.data.local.entity.PlayerEntity
import com.example.kabaddiarena.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerPerformance(
    val player: PlayerEntity?,
    val events: List<MatchEventEntity>,
    val totalPoints: Int,
    val raidSuccess: Float,
    val tacklePoints: Int,
    val coachingTip: String,
    val pointsTimeline: List<Int> = emptyList()
)

data class PerformanceSummaryState(
    val homeTeam: String = "",
    val opponentTeam: String = "",
    val teamPerformance: PlayerPerformance? = null,
    val playerPerformances: List<PlayerPerformance> = emptyList(),
    val selectedPlayerId: Long? = null // null for Team
)

@HiltViewModel
class PerformanceSummaryViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceSummaryState())
    val uiState: StateFlow<PerformanceSummaryState> = _uiState.asStateFlow()

    fun loadSummary(matchId: Long) {
        viewModelScope.launch {
            val match = repository.getMatch(matchId)
            combine(
                repository.getEventsForMatch(matchId),
                repository.getAllPlayers()
            ) { events, allPlayers ->
                val teamPerf = calculatePlayerPerformance(null, events)
                
                val playerIdsInMatch = events.mapNotNull { it.playerId }.distinct()
                val playersInMatch = allPlayers.filter { it.id in playerIdsInMatch }
                
                val playerPerfs = playersInMatch.map { player ->
                    calculatePlayerPerformance(player, events.filter { it.playerId == player.id })
                }

                PerformanceSummaryState(
                    homeTeam = match?.homeTeam ?: "",
                    opponentTeam = match?.opponentTeam ?: "",
                    teamPerformance = teamPerf,
                    playerPerformances = playerPerfs,
                    selectedPlayerId = _uiState.value.selectedPlayerId
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectPlayer(playerId: Long?) {
        _uiState.update { it.copy(selectedPlayerId = playerId) }
    }

    private fun calculatePlayerPerformance(player: PlayerEntity?, events: List<MatchEventEntity>): PlayerPerformance {
        val raidEvents = events.filter { it.type == "RAID" }
        val successfulRaids = raidEvents.filter { it.result != "FAILURE" && it.result != "EMPTY" }.size
        val totalRaids = raidEvents.size
        
        val raidPoints = raidEvents.sumOf { it.points }
        val tacklePoints = events.filter { it.type == "TACKLE" }.sumOf { it.points }
        val raidSuccessRate = if (totalRaids > 0) (successfulRaids.toFloat() / totalRaids) * 100 else 0f

        // Create a running total for the graph
        var runningTotal = 0
        val timeline = events.sortedBy { it.timestamp }.map {
            runningTotal += it.points
            runningTotal
        }

        return PlayerPerformance(
            player = player,
            events = events,
            totalPoints = raidPoints + tacklePoints,
            raidSuccess = raidSuccessRate,
            tacklePoints = tacklePoints,
            coachingTip = generateCoachingTip(raidSuccessRate, tacklePoints),
            pointsTimeline = timeline
        )
    }

    private fun generateCoachingTip(raidSuccess: Float, tacklePoints: Int): String {
        return when {
            raidSuccess == 0f && tacklePoints == 0 -> "No data available for this selection."
            raidSuccess < 40 -> "Focus on agility and 'Escape' maneuvers. Your raider is getting caught frequently."
            tacklePoints < 0 -> "Defense is leaking points! Focus on positioning and avoid premature tackle attempts."
            tacklePoints < 2 -> "Work on your 'Ankle Hold' timing. Defenders need more patience."
            raidSuccess > 70 -> "Excellent raiding! Consider attempting more 'Super Raids'."
            else -> "Steady performance. Keep working on team coordination during tackles."
        }
    }
}
