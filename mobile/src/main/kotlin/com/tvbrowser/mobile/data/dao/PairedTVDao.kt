package com.tvbrowser.mobile.data.dao

import androidx.room.*
import com.tvbrowser.mobile.data.entity.PairedTVEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for paired TV devices
 */
@Dao
interface PairedTVDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tv: PairedTVEntity): Long

    @Update
    suspend fun update(tv: PairedTVEntity)

    @Delete
    suspend fun delete(tv: PairedTVEntity)

    @Query("SELECT * FROM paired_tvs ORDER BY lastConnected DESC")
    suspend fun getAll(): List<PairedTVEntity>

    @Query("SELECT * FROM paired_tvs ORDER BY lastConnected DESC")
    fun getAllFlow(): Flow<List<PairedTVEntity>>

    @Query("SELECT * FROM paired_tvs WHERE deviceId = :deviceId")
    suspend fun getById(deviceId: String): PairedTVEntity?

    @Query("SELECT * FROM paired_tvs WHERE ipAddress = :ipAddress LIMIT 1")
    suspend fun getByIpAddress(ipAddress: String): PairedTVEntity?

    @Query("UPDATE paired_tvs SET lastConnected = :timestamp WHERE deviceId = :deviceId")
    suspend fun updateLastConnected(deviceId: String, timestamp: Long)

    @Query("DELETE FROM paired_tvs WHERE deviceId = :deviceId")
    suspend fun deleteById(deviceId: String)

    @Query("DELETE FROM paired_tvs")
    suspend fun deleteAll()
}
