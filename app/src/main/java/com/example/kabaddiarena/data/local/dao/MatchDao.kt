package com.example.kabaddiarena.data.local.dao

import androidx.room.*
import com.example.kabaddiarena.data.local.entity.MatchEntity
import com.example.kabaddiarena.data.local.entity.MatchEventEntity
import com.example.kabaddiarena.data.local.entity.PlayerEntity
import com.example.kabaddiarena.data.local.entity.PlayerStats
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: MatchEntity): Long

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Query("SELECT * FROM matches ORDER BY date DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert
    suspend fun insertEvent(event: MatchEventEntity)

    @Query("SELECT * FROM match_events WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun getEventsForMatch(matchId: Long): Flow<List<MatchEventEntity>>

    @Delete
    suspend fun deleteEvent(event: MatchEventEntity)

    @Query("SELECT * FROM match_events WHERE matchId = :matchId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEventForMatch(matchId: Long): MatchEventEntity?

    @Query("UPDATE matches SET isFinished = 1 WHERE id = :matchId")
    suspend fun markMatchAsFinished(matchId: Long)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: Long)

    @Query("DELETE FROM match_events WHERE matchId = :matchId")
    suspend fun deleteEventsByMatchId(matchId: Long)

    @Insert
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("""
        SELECT p.name, p.jerseyNumber, SUM(e.points) as totalPoints 
        FROM players p 
        JOIN match_events e ON p.id = e.playerId 
        GROUP BY p.id 
        ORDER BY totalPoints DESC 
        LIMIT 5
    """)
    fun getTopPerformers(): Flow<List<PlayerStats>>
}
