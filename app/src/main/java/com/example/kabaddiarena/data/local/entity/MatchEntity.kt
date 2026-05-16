package com.example.kabaddiarena.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val homeTeam: String,
    val opponentTeam: String,
    val date: Long,
    val isFinished: Boolean = false,
    val bestRaidVideoUrl: String? = null
)
