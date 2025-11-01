package com.tvbrowser.mobile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a paired TV device
 */
@Entity(tableName = "paired_tvs")
data class PairedTVEntity(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int = 8888,
    val pin: String? = null,
    val lastConnected: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
