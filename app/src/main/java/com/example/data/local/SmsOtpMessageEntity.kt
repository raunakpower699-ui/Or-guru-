package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_otp_messages")
data class SmsOtpMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val sender: String,
    val serviceName: String,
    val otpCode: String,
    val messageBody: String,
    val receivedTimestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isVerified: Boolean = true
)
