package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virtual_numbers")
data class VirtualNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val rawDigits: String,
    val countryCode: String,
    val countryName: String,
    val countryFlag: String,
    val carrier: String,
    val status: String = "ACTIVE", // ACTIVE, EXPIRED, COOLDOWN
    val assignedTimestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val isFavorite: Boolean = false,
    val signalStrength: Int = 95,
    val smsCount: Int = 0,
    val activationId: String? = null,
    val serviceCode: String = "wa",
    val isRealApiRental: Boolean = false,
    val rentalStatus: String = "ACTIVE" // WAIT_CODE, CODE_RECEIVED, FINISHED, CANCELED
)
