package com.example.faceit.di

import android.content.Context
import androidx.room.Room
import com.example.faceit.data.local.FaceitDatabase
import com.example.faceit.data.local.InitialDataSeed
import com.example.faceit.data.local.MatchDao
import com.example.faceit.data.local.PlayerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FaceitDatabase {
        val db = Room.databaseBuilder(context, FaceitDatabase::class.java, "faceit_scout.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
        runBlocking(Dispatchers.IO) {
            InitialDataSeed.ensureDemoData(db.playerDao(), db.matchDao())
        }
        return db
    }

    @Provides
    fun providePlayerDao(db: FaceitDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideMatchDao(db: FaceitDatabase): MatchDao = db.matchDao()
}
