package com.example.kabaddiarena.di

import android.content.Context
import androidx.room.Room
import com.example.kabaddiarena.data.local.AppDatabase
import com.example.kabaddiarena.data.local.dao.MatchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kabaddi_arena_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMatchDao(database: AppDatabase): MatchDao {
        return database.matchDao()
    }
}
