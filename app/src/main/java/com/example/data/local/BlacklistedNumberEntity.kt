package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklisted_numbers")
data class BlacklistedNumberEntity(
    @PrimaryKey
    val phoneNumber: String,
    val rawDigits: String,
    val countryCode: String,
    val blacklistedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val reason: String = "SHOWN_IN_24H_QUOTA"
)
