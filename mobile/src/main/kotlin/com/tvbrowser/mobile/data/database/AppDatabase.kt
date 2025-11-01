package com.tvbrowser.mobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvbrowser.mobile.data.dao.BrowsingHistoryDao
import com.tvbrowser.mobile.data.dao.PairedTVDao
import com.tvbrowser.mobile.data.entity.BrowsingHistory
import com.tvbrowser.mobile.data.entity.PairedTV

@Database(
    entities = [BrowsingHistory::class, PairedTV::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun browsingHistoryDao(): BrowsingHistoryDao
    abstract fun pairedTVDao(): PairedTVDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "browsesnap_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
