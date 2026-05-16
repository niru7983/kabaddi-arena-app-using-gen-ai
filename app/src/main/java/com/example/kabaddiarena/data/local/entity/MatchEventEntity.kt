package com.example.kabaddiarena.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_events")
data class MatchEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val playerId: Long? = null,
    val type: String, // "RAID", "TACKLE"
    val result: String, // "SUCCESS", "FAILURE", "EMPTY", "TOUCH", "BONUS", "SUPER_TACKLE"
    val points: Int,
    val x: Float? = null,
    val y: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
