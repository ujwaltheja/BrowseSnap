package com.tvbrowser.mobile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing browsing history stored in local database
 */
@Entity(tableName = "browsing_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String = "",
    val action: String, // "open_url", "play_video", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null,
    val deviceId: String? = null
)
