package com.example.kabaddiarena.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kabaddiarena.data.local.dao.MatchDao
import com.example.kabaddiarena.data.local.entity.MatchEntity
import com.example.kabaddiarena.data.local.entity.MatchEventEntity
import com.example.kabaddiarena.data.local.entity.PlayerEntity

@Database(entities = [MatchEntity::class, MatchEventEntity::class, PlayerEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}
