package com.tvbrowser.mobile.data.repository

import com.tvbrowser.mobile.data.dao.HistoryDao
import com.tvbrowser.mobile.data.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing browsing history data
 */
@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {

    /**
     * Add a new history entry
     */
    suspend fun addHistory(
        url: String,
        title: String = "",
        action: String,
        thumbnailUrl: String? = null,
        deviceId: String? = null
    ): Long {
        val history = HistoryEntity(
            url = url,
            title = title,
            action = action,
            thumbnailUrl = thumbnailUrl,
            deviceId = deviceId,
            timestamp = System.currentTimeMillis()
        )
        return historyDao.insert(history)
    }

    /**
     * Get recent history entries
     */
    suspend fun getRecentHistory(limit: Int = 50): List<HistoryEntity> {
        return historyDao.getRecent(limit)
    }

    /**
     * Get recent history as Flow for reactive updates
     */
    fun getRecentHistoryFlow(limit: Int = 50): Flow<List<HistoryEntity>> {
        return historyDao.getRecentFlow(limit)
    }

    /**
     * Search history by query
     */
    suspend fun searchHistory(query: String): List<HistoryEntity> {
        val searchQuery = "%$query%"
        return historyDao.search(searchQuery)
    }

    /**
     * Get history by ID
     */
    suspend fun getHistoryById(id: Long): HistoryEntity? {
        return historyDao.getById(id)
    }

    /**
     * Delete a history entry
     */
    suspend fun deleteHistory(history: HistoryEntity) {
        historyDao.delete(history)
    }

    /**
     * Delete history entries older than specified timestamp
     */
    suspend fun deleteOlderThan(timestamp: Long) {
        historyDao.deleteOlderThan(timestamp)
    }

    /**
     * Delete all history
     */
    suspend fun clearAll() {
        historyDao.deleteAll()
    }

    /**
     * Get total count of history entries
     */
    suspend fun getCount(): Int {
        return historyDao.getCount()
    }

    /**
     * Get history by action type
     */
    suspend fun getByAction(action: String, limit: Int = 20): List<HistoryEntity> {
        return historyDao.getByAction(action, limit)
    }
}
