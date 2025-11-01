package com.tvbrowser.mobile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browsing_history")
data class BrowsingHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
    val title: String? = null,
    val action: String, // "open_url" or "play_video"
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null,
    val deviceId: String? = null
)
