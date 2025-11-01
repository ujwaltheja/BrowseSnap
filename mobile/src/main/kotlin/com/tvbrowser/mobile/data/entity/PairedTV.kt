package com.tvbrowser.mobile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_tvs")
data class PairedTV(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int = 8888,
    val pin: String? = null,
    val authToken: String? = null,
    val lastConnected: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
