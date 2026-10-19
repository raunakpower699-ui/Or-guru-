package com.example.data.repository

import com.example.data.api.CountryTarget
import com.example.data.api.RentedNumberResult
import com.example.data.api.ServiceTarget
import com.example.data.api.SmsActivationApi
import com.example.data.api.SmsStatusResult
import com.example.data.local.AppDatabase
import com.example.data.local.BlacklistedNumberEntity
import com.example.data.local.SmsOtpMessageEntity
import com.example.data.local.VirtualNumberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NumberRepository(
    private val database: AppDatabase,
    private val activationApi: SmsActivationApi = SmsActivationApi()
) {

    private val virtualNumberDao = database.virtualNumberDao()
    private val blacklistedNumberDao = database.blacklistedNumberDao()
    private val smsOtpDao = database.smsOtpDao()

    private val mutex = Mutex()
    private val _auditLogs = MutableStateFlow<List<String>>(emptyList())
    val auditLogs: StateFlow<List<String>> = _auditLogs.asStateFlow()

    val activeNumbers: Flow<List<VirtualNumberEntity>> = virtualNumberDao.getActiveNumbersFlow()
    val allNumbers: Flow<List<VirtualNumberEntity>> = virtualNumberDao.getAllNumbersFlow()
    val allBlacklisted: Flow<List<BlacklistedNumberEntity>> = blacklistedNumberDao.getAllBlacklistedFlow()

    init {
        logAudit("SYSTEM", "Raunak Exploits Kernel v3.4 online. Anti-loop shield & SMS API ready.")
    }

    fun logAudit(tag: String, message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val entry = "[$time] [$tag] $message"
        _auditLogs.value = (_auditLogs.value + entry).takeLast(150)
    }

    suspend fun getActiveNumbersCount(): Int {
        return virtualNumberDao.getActiveNumbersCount()
    }

    suspend fun getActiveBlacklistCount(): Int {
        return blacklistedNumberDao.getActiveBlacklistCount(System.currentTimeMillis())
    }

    suspend fun getNumberByPhone(phone: String): VirtualNumberEntity? {
        return virtualNumberDao.getNumberByPhone(phone)
    }

    fun getMessagesForNumber(phoneNumber: String): Flow<List<SmsOtpMessageEntity>> {
        return smsOtpDao.getMessagesForNumberFlow(phoneNumber)
    }

    // =========================================================================
    // Real SMS Activation API (SMS-Activate / 5SIM Compatible)
    // =========================================================================

    /**
     * Action 1: Check real account balance & API connectivity
     */
    suspend fun checkApiBalance(
        apiKey: String,
        baseUrl: String = SmsActivationApi.DEFAULT_BASE_URL
    ): Result<Pair<Double, String>> {
        val maskedKey = if (apiKey.length > 8) "${apiKey.take(4)}...${apiKey.takeLast(4)}" else apiKey
        logAudit("API:REQ", "GET getBalance [Key: $maskedKey] -> $baseUrl")
        val result = activationApi.getBalance(apiKey, baseUrl)
        result.onSuccess { (balance, raw) ->
            logAudit("API:RES", "Balance: $$balance [RAW: $raw]")
        }.onFailure { err ->
            logAudit("API:ERR", "getBalance Failed: ${err.message}")
        }
        return result
    }

    /**
     * Action 2: Rent a real working number from the activation API
     */
    suspend fun rentRealNumber(
        serviceCode: String,
        countryId: String,
        apiKey: String,
        baseUrl: String = SmsActivationApi.DEFAULT_BASE_URL
    ): Result<VirtualNumberEntity> = mutex.withLock {
        val serviceName = SmsActivationApi.AVAILABLE_SERVICES.find { it.code == serviceCode }?.name ?: serviceCode.uppercase()
        val country = SmsActivationApi.AVAILABLE_COUNTRIES.find { it.id == countryId }
            ?: CountryTarget(countryId, "Country $countryId", "🌐", "+")

        val maskedKey = if (apiKey.length > 8) "${apiKey.take(4)}...${apiKey.takeLast(4)}" else apiKey
        logAudit("API:REQ", "GET getNumber [Service: $serviceCode ($serviceName), Country: $countryId (${country.name}), Key: $maskedKey]")

        val result = activationApi.getNumber(serviceCode, countryId, apiKey, baseUrl)

        return@withLock if (result.isSuccess) {
            val rented = result.getOrThrow()
            logAudit("API:RES", "ACCESS_NUMBER -> ID: ${rented.activationId}, Phone: ${rented.phoneNumber} [RAW: ${rented.rawResponse}]")

            val now = System.currentTimeMillis()
            val rawDigits = rented.phoneNumber.filter { it.isDigit() }

            val entity = VirtualNumberEntity(
                phoneNumber = rented.phoneNumber,
                rawDigits = rawDigits,
                countryCode = country.name.take(2).uppercase(),
                countryName = country.name,
                countryFlag = country.flag,
                carrier = "Real GSM Node (${country.name})",
                status = "ACTIVE",
                assignedTimestamp = now,
                expiresAt = now + 20 * 60 * 1000L, // 20-minute SMS activation window
                signalStrength = 100,
                smsCount = 0,
                activationId = rented.activationId,
                serviceCode = serviceCode,
                isRealApiRental = true,
                rentalStatus = "WAIT_CODE"
            )

            // Persist to Room & add to 24-hour anti-loop blacklist
            virtualNumberDao.insertNumber(entity)
            blacklistedNumberDao.insertBlacklistOne(
                BlacklistedNumberEntity(
                    phoneNumber = entity.phoneNumber,
                    rawDigits = entity.rawDigits,
                    countryCode = entity.countryCode,
                    blacklistedAt = now,
                    expiresAt = now + 24 * 60 * 60 * 1000L,
                    reason = "RENTED_REAL_API_SLOT"
                )
            )

            logAudit("ANTI-LOOP", "Rented node ${entity.phoneNumber} (ID: ${rented.activationId}) locked into blacklist.")
            Result.success(entity)
        } else {
            val error = result.exceptionOrNull()
            logAudit("API:ERR", "getNumber Failed: ${error?.message}")
            Result.failure(error ?: Exception("Unknown error renting number"))
        }
    }

    /**
     * Action 3: Poll/Check status for incoming real SMS/OTP on rented ID
     */
    suspend fun pollRealSmsStatus(
        activationId: String,
        phoneNumber: String,
        serviceName: String,
        apiKey: String,
        baseUrl: String = SmsActivationApi.DEFAULT_BASE_URL
    ): Result<SmsStatusResult> {
        val result = activationApi.getStatus(activationId, apiKey, baseUrl)

        result.onSuccess { status ->
            when (status) {
                is SmsStatusResult.WaitingForCode -> {
                    // Still waiting - do not spam logs, but keep state active
                }
                is SmsStatusResult.CodeReceived -> {
                    logAudit("API:OTP", "STATUS_OK RECEIVED! Code: [${status.code}] for Phone: $phoneNumber (ID: $activationId)")
                    // Persist real received OTP to Room Database
                    val msg = SmsOtpMessageEntity(
                        phoneNumber = phoneNumber,
                        sender = "$serviceName-REAL",
                        serviceName = serviceName,
                        otpCode = status.code,
                        messageBody = status.fullMessage,
                        receivedTimestamp = System.currentTimeMillis(),
                        isRead = false,
                        isVerified = true
                    )
                    smsOtpDao.insertMessage(msg)
                    virtualNumberDao.updateRentalStatus(activationId, "CODE_RECEIVED")
                    virtualNumberDao.updateAllSmsCounts()
                }
                is SmsStatusResult.Canceled -> {
                    logAudit("API:RES", "Activation ID $activationId was canceled by server.")
                    virtualNumberDao.updateRentalStatus(activationId, "CANCELED")
                }
                is SmsStatusResult.Error -> {
                    logAudit("API:ERR", "Status poll error for $activationId: ${status.message} [RAW: ${status.rawResponse}]")
                }
            }
        }.onFailure { err ->
            logAudit("API:ERR", "Network poll failed for $activationId: ${err.message}")
        }

        return result
    }

    /**
     * Action 4: Cancel or Finish real activation on SMS API
     */
    suspend fun cancelRealActivation(
        activationId: String,
        apiKey: String,
        baseUrl: String = SmsActivationApi.DEFAULT_BASE_URL
    ): Result<String> {
        logAudit("API:REQ", "GET setStatus (8 = Cancel) -> ID: $activationId")
        val result = activationApi.setStatus(activationId, 8, apiKey, baseUrl)
        result.onSuccess { raw ->
            logAudit("API:RES", "Activation $activationId canceled [RAW: $raw]")
            virtualNumberDao.updateRentalStatus(activationId, "CANCELED")
        }.onFailure { err ->
            logAudit("API:ERR", "Cancel failed: ${err.message}")
        }
        return result
    }

    suspend fun finishRealActivation(
        activationId: String,
        apiKey: String,
        baseUrl: String = SmsActivationApi.DEFAULT_BASE_URL
    ): Result<String> {
        logAudit("API:REQ", "GET setStatus (6 = Finish) -> ID: $activationId")
        val result = activationApi.setStatus(activationId, 6, apiKey, baseUrl)
        result.onSuccess { raw ->
            logAudit("API:RES", "Activation $activationId finished [RAW: $raw]")
            virtualNumberDao.updateRentalStatus(activationId, "FINISHED")
        }.onFailure { err ->
            logAudit("API:ERR", "Finish failed: ${err.message}")
        }
        return result
    }

    // =========================================================================
    // Core Anti-Loop & 24h Quota Engine
    // =========================================================================

    suspend fun ensureDailyQuota(forceRefresh: Boolean = false): Int = mutex.withLock {
        val now = System.currentTimeMillis()
        logAudit("ANTI-LOOP", "Verifying daily quota status (force=$forceRefresh)...")

        val purgedBlacklist = blacklistedNumberDao.purgeExpiredBlacklist(now)
        if (purgedBlacklist > 0) {
            logAudit("PURGE", "Purged $purgedBlacklist expired blacklist hashes.")
        }

        val expiredCount = virtualNumberDao.markExpiredNumbers(now)
        if (expiredCount > 0) {
            logAudit("LIFECYCLE", "$expiredCount virtual numbers marked EXPIRED.")
        }

        if (forceRefresh) {
            val currentActives = virtualNumberDao.getActiveNumbersList()
            if (currentActives.isNotEmpty()) {
                val blacklistEntries = currentActives.map { num ->
                    BlacklistedNumberEntity(
                        phoneNumber = num.phoneNumber,
                        rawDigits = num.rawDigits,
                        countryCode = num.countryCode,
                        blacklistedAt = now,
                        expiresAt = now + 24 * 60 * 60 * 1000L,
                        reason = "MANUAL_OR_AUTO_CYCLE_PURGE"
                    )
                }
                blacklistedNumberDao.insertBlacklist(blacklistEntries)
                virtualNumberDao.markExpiredNumbers(now + 9999999999L)
                logAudit("ANTI-LOOP", "Blacklisted ${blacklistEntries.size} previous numbers for 24 hours to prevent loops.")
            }
        }

        val activeList = virtualNumberDao.getActiveNumbersList()
        val needed = 10 - activeList.size

        if (needed <= 0) {
            logAudit("STATUS", "Quota intact: ${activeList.size}/10 active numbers ready. Zero repeats detected.")
            return@withLock activeList.size
        }

        logAudit("PROVISION", "Allocating $needed fresh virtual numbers from secure pool...")

        val blacklistedRawDigits = blacklistedNumberDao.getActiveBlacklistedRawDigits(now).toSet()
        val existingRawDigits = activeList.map { it.rawDigits }.toSet()

        val newNumbers = mutableListOf<VirtualNumberEntity>()
        val newBlacklistEntries = mutableListOf<BlacklistedNumberEntity>()
        val initialOtps = mutableListOf<SmsOtpMessageEntity>()

        val preferredCountries = listOf("US", "GB", "IN", "CA", "DE", "FR", "NL", "AU")

        for (i in 0 until needed) {
            val countryPref = preferredCountries[i % preferredCountries.size]
            val candidate = NumberPoolGenerator.generateFreshCandidate(
                blacklistedRawDigits = blacklistedRawDigits + newNumbers.map { it.rawDigits }.toSet(),
                existingRawDigits = existingRawDigits + newNumbers.map { it.rawDigits }.toSet(),
                preferredCountryCode = countryPref
            )

            newNumbers.add(candidate)

            newBlacklistEntries.add(
                BlacklistedNumberEntity(
                    phoneNumber = candidate.phoneNumber,
                    rawDigits = candidate.rawDigits,
                    countryCode = candidate.countryCode,
                    blacklistedAt = now,
                    expiresAt = now + 24 * 60 * 60 * 1000L,
                    reason = "ALLOCATED_TO_ACTIVE_QUOTA"
                )
            )

            val sampleOtps = NumberPoolGenerator.generateInitialOtpsForNumber(candidate.phoneNumber, count = 1)
            initialOtps.addAll(sampleOtps)
        }

        blacklistedNumberDao.insertBlacklist(newBlacklistEntries)
        virtualNumberDao.insertNumbers(newNumbers)
        smsOtpDao.insertMessages(initialOtps)
        virtualNumberDao.updateAllSmsCounts()

        logAudit("SUCCESS", "Provisioned $needed fresh numbers. Zero repetition guaranteed.")
        return@withLock (activeList.size + needed)
    }

    suspend fun injectIncomingOtp(
        phoneNumber: String,
        serviceName: String? = null,
        customCode: String? = null
    ): SmsOtpMessageEntity {
        val message = NumberPoolGenerator.generateOtpForService(
            phoneNumber = phoneNumber,
            serviceName = serviceName,
            customCode = customCode
        )
        smsOtpDao.insertMessage(message)
        virtualNumberDao.updateAllSmsCounts()
        logAudit("INBOX", "Incoming OTP received on $phoneNumber from ${message.serviceName} [CODE: ${message.otpCode}]")
        return message
    }

    suspend fun markOtpRead(id: Long) {
        smsOtpDao.markAsRead(id)
    }

    suspend fun clearMessagesForNumber(phoneNumber: String) {
        smsOtpDao.deleteMessagesForNumber(phoneNumber)
        virtualNumberDao.updateAllSmsCounts()
        logAudit("INBOX", "Cleared SMS history for $phoneNumber")
    }

    suspend fun getCycleExpiryTimestamp(): Long {
        val active = virtualNumberDao.getActiveNumbersList()
        return active.minOfOrNull { it.expiresAt } ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
    }

    suspend fun purgeAllAndReset(): Unit = mutex.withLock {
        virtualNumberDao.deleteAllNumbers()
        smsOtpDao.clearAllMessages()
        logAudit("RESET", "Hard reset performed on virtual numbers cache.")
        ensureDailyQuota(forceRefresh = true)
    }
}
