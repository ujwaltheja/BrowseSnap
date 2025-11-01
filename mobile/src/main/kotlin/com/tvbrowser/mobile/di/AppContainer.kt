package com.tvbrowser.mobile.di

import android.content.Context
import com.tvbrowser.mobile.data.database.BrowseSnapDatabase
import com.tvbrowser.mobile.data.repository.HistoryRepository
import com.tvbrowser.mobile.data.repository.PairedTVRepository
import com.tvbrowser.mobile.data.repository.TVConnectionRepository

/**
 * Simple dependency injection container for the app
 * This provides all dependencies needed throughout the app
 */
class AppContainer(context: Context) {

    // Database
    private val database: BrowseSnapDatabase by lazy {
        BrowseSnapDatabase.getInstance(context.applicationContext)
    }

    // DAOs
    private val historyDao by lazy { database.historyDao() }
    private val pairedTVDao by lazy { database.pairedTVDao() }

    // Repositories
    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(historyDao)
    }

    val pairedTVRepository: PairedTVRepository by lazy {
        PairedTVRepository(pairedTVDao)
    }

    val tvConnectionRepository: TVConnectionRepository by lazy {
        TVConnectionRepository()
    }
}
