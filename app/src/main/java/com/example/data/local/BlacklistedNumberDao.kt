package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistedNumberDao {
    @Query("SELECT * FROM blacklisted_numbers ORDER BY blacklistedAt DESC")
    fun getAllBlacklistedFlow(): Flow<List<BlacklistedNumberEntity>>

    @Query("SELECT phoneNumber FROM blacklisted_numbers WHERE expiresAt > :currentTime")
    suspend fun getActiveBlacklistedPhones(currentTime: Long): List<String>

    @Query("SELECT rawDigits FROM blacklisted_numbers WHERE expiresAt > :currentTime")
    suspend fun getActiveBlacklistedRawDigits(currentTime: Long): List<String>

    @Query("SELECT COUNT(*) FROM blacklisted_numbers WHERE (phoneNumber = :phoneNumber OR rawDigits = :rawDigits) AND expiresAt > :currentTime")
    suspend fun isBlacklisted(phoneNumber: String, rawDigits: String, currentTime: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklist(entries: List<BlacklistedNumberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklistOne(entry: BlacklistedNumberEntity)

    @Query("DELETE FROM blacklisted_numbers WHERE expiresAt <= :currentTime")
    suspend fun purgeExpiredBlacklist(currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM blacklisted_numbers WHERE expiresAt > :currentTime")
    suspend fun getActiveBlacklistCount(currentTime: Long): Int

    @Query("DELETE FROM blacklisted_numbers")
    suspend fun clearAll()
}
