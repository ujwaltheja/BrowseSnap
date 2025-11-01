package com.tvbrowser.mobile.data.dao

import androidx.room.*
import com.tvbrowser.mobile.data.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for browsing history
 */
@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: HistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<HistoryEntity>)

    @Update
    suspend fun update(history: HistoryEntity)

    @Delete
    suspend fun delete(history: HistoryEntity)

    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<HistoryEntity>

    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 50): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM browsing_history WHERE url LIKE :query OR title LIKE :query ORDER BY timestamp DESC")
    suspend fun search(query: String): List<HistoryEntity>

    @Query("SELECT * FROM browsing_history WHERE id = :id")
    suspend fun getById(id: Long): HistoryEntity?

    @Query("DELETE FROM browsing_history WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)

    @Query("DELETE FROM browsing_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM browsing_history")
    suspend fun getCount(): Int

    @Query("SELECT * FROM browsing_history WHERE action = :action ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByAction(action: String, limit: Int = 20): List<HistoryEntity>
}
