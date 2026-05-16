package com.example.kabaddiarena.ui.logger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddiarena.data.local.entity.MatchEventEntity
import com.example.kabaddiarena.data.local.entity.PlayerEntity
import com.example.kabaddiarena.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveLoggerUiState(
    val matchId: Long = -1,
    val homeTeam: String = "",
    val opponentTeam: String = "",
    val events: List<MatchEventEntity> = emptyList(),
    val allPlayers: List<PlayerEntity> = emptyList(),
    val squadPlayerIds: Set<Long> = emptySet(),
    val selectedPlayerId: Long? = null,
    val totalRaidPoints: Int = 0,
    val totalTacklePoints: Int = 0,
    val raidSuccessRate: Float = 0f,
    val isFinished: Boolean = false,
) {
    val squadPlayers: List<PlayerEntity>
        get() = allPlayers.filter { it.id in squadPlayerIds }
}

@HiltViewModel
class LiveLoggerViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveLoggerUiState())
    val uiState: StateFlow<LiveLoggerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllPlayers().collect { players ->
                _uiState.update { it.copy(allPlayers = players) }
            }
        }
    }

    fun togglePlayerInSquad(playerId: Long) {
        _uiState.update { state ->
            val newSquad = if (state.squadPlayerIds.contains(playerId)) {
                state.squadPlayerIds - playerId
            } else {
                state.squadPlayerIds + playerId
            }
            
            // Auto-select first player if none selected and squad is not empty
            val newSelectedId = if (state.selectedPlayerId == null || (state.selectedPlayerId != null && !newSquad.contains(state.selectedPlayerId))) {
                newSquad.firstOrNull()
            } else {
                state.selectedPlayerId
            }
            
            state.copy(squadPlayerIds = newSquad, selectedPlayerId = newSelectedId)
        }
    }

    fun setMatch(matchId: Long) {
        viewModelScope.launch {
            val match = repository.getMatch(matchId)
            _uiState.update { it.copy(
                matchId = matchId,
                homeTeam = match?.homeTeam ?: "",
                opponentTeam = match?.opponentTeam ?: "",
                isFinished = match?.isFinished ?: false
            ) }

            repository.getEventsForMatch(matchId).collect { events ->
                calculateStats(matchId, events)
            }
        }
    }

    fun selectPlayer(playerId: Long?) {
        _uiState.update { it.copy(selectedPlayerId = playerId) }
    }

    private fun calculateStats(matchId: Long, events: List<MatchEventEntity>) {
        val raidEvents = events.filter { it.type == "RAID" }
        val successfulRaids = raidEvents.filter { it.result != "FAILURE" && it.result != "EMPTY" }.size
        val totalRaids = raidEvents.size
        
        val raidPoints = raidEvents.sumOf { it.points }
        val tacklePoints = events.filter { it.type == "TACKLE" }.sumOf { it.points }

        _uiState.update { 
            it.copy(
                matchId = matchId,
                events = events,
                totalRaidPoints = raidPoints,
                totalTacklePoints = tacklePoints,
                raidSuccessRate = if (totalRaids > 0) (successfulRaids.toFloat() / totalRaids) * 100 else 0f
            )
        }
    }

    fun logRaid(result: String, points: Int, x: Float? = null, y: Float? = null) {
        if (_uiState.value.isFinished) return
        viewModelScope.launch {
            val event = MatchEventEntity(
                matchId = _uiState.value.matchId,
                playerId = _uiState.value.selectedPlayerId,
                type = "RAID",
                result = result,
                points = points,
                x = x,
                y = y
            )
            repository.logEvent(event)
        }
    }

    fun logTackle(result: String, points: Int, x: Float? = null, y: Float? = null) {
        if (_uiState.value.isFinished) return
        viewModelScope.launch {
            val event = MatchEventEntity(
                matchId = _uiState.value.matchId,
                playerId = _uiState.value.selectedPlayerId,
                type = "TACKLE",
                result = result,
                points = points,
                x = x,
                y = y
            )
            repository.logEvent(event)
        }
    }

    fun addQuickPlayer(name: String, jersey: String) {
        viewModelScope.launch {
            repository.addPlayer(name, jersey, "Raider")
        }
    }

    fun undo() {
        if (_uiState.value.isFinished) return
        viewModelScope.launch {
            repository.undoLastEvent(_uiState.value.matchId)
        }
    }

    fun finishMatch(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.finishMatch(_uiState.value.matchId)
            _uiState.update { it.copy(isFinished = true) }
            onComplete()
        }
    }
}
