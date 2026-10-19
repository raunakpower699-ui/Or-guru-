package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsOtpDao {
    @Query("SELECT * FROM sms_otp_messages WHERE phoneNumber = :phoneNumber ORDER BY receivedTimestamp DESC")
    fun getMessagesForNumberFlow(phoneNumber: String): Flow<List<SmsOtpMessageEntity>>

    @Query("SELECT * FROM sms_otp_messages ORDER BY receivedTimestamp DESC")
    fun getAllMessagesFlow(): Flow<List<SmsOtpMessageEntity>>

    @Query("SELECT COUNT(*) FROM sms_otp_messages WHERE phoneNumber = :phoneNumber")
    suspend fun getMessageCountForNumber(phoneNumber: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsOtpMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<SmsOtpMessageEntity>)

    @Query("UPDATE sms_otp_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM sms_otp_messages WHERE phoneNumber = :phoneNumber")
    suspend fun deleteMessagesForNumber(phoneNumber: String)

    @Query("DELETE FROM sms_otp_messages")
    suspend fun clearAllMessages()
}
