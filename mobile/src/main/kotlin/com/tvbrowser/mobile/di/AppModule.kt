package com.tvbrowser.mobile.di

import android.content.Context
import com.tvbrowser.mobile.data.database.AppDatabase
import com.tvbrowser.mobile.data.repository.TVRepository

object AppModule {

    private lateinit var database: AppDatabase
    private lateinit var repository: TVRepository

    fun initialize(context: Context) {
        database = AppDatabase.getDatabase(context)
        repository = TVRepository(
            pairedTVDao = database.pairedTVDao(),
            browsingHistoryDao = database.browsingHistoryDao()
        )
    }

    fun provideRepository(): TVRepository {
        if (!::repository.isInitialized) {
            throw IllegalStateException("AppModule not initialized")
        }
        return repository
    }

    fun provideDatabase(): AppDatabase {
        if (!::database.isInitialized) {
            throw IllegalStateException("AppModule not initialized")
        }
        return database
    }
}
