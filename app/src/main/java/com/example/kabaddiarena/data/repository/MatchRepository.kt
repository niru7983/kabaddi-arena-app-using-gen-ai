package com.example.kabaddiarena.data.repository

import com.example.kabaddiarena.data.local.dao.MatchDao
import com.example.kabaddiarena.data.local.entity.MatchEntity
import com.example.kabaddiarena.data.local.entity.MatchEventEntity
import com.example.kabaddiarena.data.local.entity.PlayerEntity
import com.example.kabaddiarena.data.local.entity.PlayerStats
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val matchDao: MatchDao
) {
    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatches()

    suspend fun getMatch(matchId: Long): MatchEntity? = matchDao.getMatchById(matchId)

    suspend fun createMatch(homeTeam: String, opponentTeam: String): Long {
        val match = MatchEntity(homeTeam = homeTeam, opponentTeam = opponentTeam, date = System.currentTimeMillis())
        return matchDao.insertMatch(match)
    }

    fun getEventsForMatch(matchId: Long): Flow<List<MatchEventEntity>> = matchDao.getEventsForMatch(matchId)

    suspend fun logEvent(event: MatchEventEntity) = matchDao.insertEvent(event)

    suspend fun undoLastEvent(matchId: Long) {
        val lastEvent = matchDao.getLastEventForMatch(matchId)
        if (lastEvent != null) {
            matchDao.deleteEvent(lastEvent)
        }
    }

    suspend fun deleteMatch(matchId: Long) {
        matchDao.deleteMatchById(matchId)
        matchDao.deleteEventsByMatchId(matchId)
    }

    suspend fun finishMatch(matchId: Long) {
        matchDao.markMatchAsFinished(matchId)
    }

    fun getAllPlayers(): Flow<List<PlayerEntity>> = matchDao.getAllPlayers()

    fun getTopPerformers(): Flow<List<PlayerStats>> = matchDao.getTopPerformers()

    suspend fun addPlayer(name: String, jersey: String, role: String) {
        matchDao.insertPlayer(PlayerEntity(name = name, jerseyNumber = jersey, role = role))
    }
}
