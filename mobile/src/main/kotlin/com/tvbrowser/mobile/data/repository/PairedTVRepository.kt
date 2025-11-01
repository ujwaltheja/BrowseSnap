package com.tvbrowser.mobile.data.repository

import com.tvbrowser.mobile.data.dao.PairedTVDao
import com.tvbrowser.mobile.data.entity.PairedTVEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing paired TV devices
 */
@Singleton
class PairedTVRepository @Inject constructor(
    private val pairedTVDao: PairedTVDao
) {

    /**
     * Add or update a paired TV device
     */
    suspend fun addPairedTV(
        deviceId: String,
        deviceName: String,
        ipAddress: String,
        port: Int = 8888,
        pin: String? = null
    ): Long {
        val tv = PairedTVEntity(
            deviceId = deviceId,
            deviceName = deviceName,
            ipAddress = ipAddress,
            port = port,
            pin = pin,
            lastConnected = System.currentTimeMillis()
        )
        return pairedTVDao.insert(tv)
    }

    /**
     * Get all paired TVs
     */
    suspend fun getAllPairedTVs(): List<PairedTVEntity> {
        return pairedTVDao.getAll()
    }

    /**
     * Get all paired TVs as Flow for reactive updates
     */
    fun getAllPairedTVsFlow(): Flow<List<PairedTVEntity>> {
        return pairedTVDao.getAllFlow()
    }

    /**
     * Get paired TV by device ID
     */
    suspend fun getPairedTVById(deviceId: String): PairedTVEntity? {
        return pairedTVDao.getById(deviceId)
    }

    /**
     * Get paired TV by IP address
     */
    suspend fun getPairedTVByIp(ipAddress: String): PairedTVEntity? {
        return pairedTVDao.getByIpAddress(ipAddress)
    }

    /**
     * Update last connected timestamp for a TV
     */
    suspend fun updateLastConnected(deviceId: String) {
        pairedTVDao.updateLastConnected(deviceId, System.currentTimeMillis())
    }

    /**
     * Delete a paired TV
     */
    suspend fun deletePairedTV(tv: PairedTVEntity) {
        pairedTVDao.delete(tv)
    }

    /**
     * Delete paired TV by device ID
     */
    suspend fun deletePairedTVById(deviceId: String) {
        pairedTVDao.deleteById(deviceId)
    }

    /**
     * Delete all paired TVs
     */
    suspend fun clearAll() {
        pairedTVDao.deleteAll()
    }

    /**
     * Update paired TV info
     */
    suspend fun updatePairedTV(tv: PairedTVEntity) {
        pairedTVDao.update(tv)
    }
}
