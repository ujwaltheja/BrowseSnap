package com.tvbrowser.mobile.data.dao

import androidx.room.*
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowsingHistoryDao {

    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<BrowsingHistory>>

    @Query("SELECT * FROM browsing_history WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getHistoryByDevice(deviceId: String): Flow<List<BrowsingHistory>>

    @Query("SELECT * FROM browsing_history WHERE action = :action ORDER BY timestamp DESC LIMIT :limit")
    fun getHistoryByAction(action: String, limit: Int = 50): Flow<List<BrowsingHistory>>

    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 20): Flow<List<BrowsingHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: BrowsingHistory): Long

    @Delete
    suspend fun delete(history: BrowsingHistory)

    @Query("DELETE FROM browsing_history")
    suspend fun clearAll()

    @Query("DELETE FROM browsing_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
