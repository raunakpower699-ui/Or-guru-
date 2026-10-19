package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VirtualNumberEntity::class,
        BlacklistedNumberEntity::class,
        SmsOtpMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun virtualNumberDao(): VirtualNumberDao
    abstract fun blacklistedNumberDao(): BlacklistedNumberDao
    abstract fun smsOtpDao(): SmsOtpDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "raunak_exploits_db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
