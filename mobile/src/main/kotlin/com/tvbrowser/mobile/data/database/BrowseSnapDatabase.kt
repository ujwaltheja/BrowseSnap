package com.tvbrowser.mobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvbrowser.mobile.data.dao.HistoryDao
import com.tvbrowser.mobile.data.dao.PairedTVDao
import com.tvbrowser.mobile.data.entity.HistoryEntity
import com.tvbrowser.mobile.data.entity.PairedTVEntity

/**
 * Main database for the BrowseSnap mobile app
 */
@Database(
    entities = [
        HistoryEntity::class,
        PairedTVEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrowseSnapDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun pairedTVDao(): PairedTVDao

    companion object {
        @Volatile
        private var INSTANCE: BrowseSnapDatabase? = null

        fun getInstance(context: Context): BrowseSnapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowseSnapDatabase::class.java,
                    "browsesnap_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * For testing purposes only
         */
        fun getInMemoryDatabase(context: Context): BrowseSnapDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                BrowseSnapDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
