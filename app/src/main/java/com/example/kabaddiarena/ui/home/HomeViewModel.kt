package com.example.kabaddiarena.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddiarena.data.local.entity.MatchEntity
import com.example.kabaddiarena.data.local.entity.PlayerStats
import com.example.kabaddiarena.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.kabaddiarena.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    val allMatches: StateFlow<List<MatchEntity>> = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topPerformers: StateFlow<List<PlayerStats>> = repository.getTopPerformers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allPlayers: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNewMatch(homeTeam: String, opponentName: String, onMatchCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createMatch(homeTeam, opponentName)
            onMatchCreated(id)
        }
    }

    fun deleteMatch(matchId: Long) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
        }
    }
}
