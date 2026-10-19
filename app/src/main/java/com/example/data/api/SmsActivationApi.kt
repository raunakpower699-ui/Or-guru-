package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ServiceTarget(
    val code: String,
    val name: String,
    val iconName: String,
    val description: String
)

data class CountryTarget(
    val id: String,
    val name: String,
    val flag: String,
    val dialCode: String
)

data class RentedNumberResult(
    val activationId: String,
    val phoneNumber: String,
    val serviceCode: String,
    val countryId: String,
    val rawResponse: String
)

sealed class SmsStatusResult {
    data object WaitingForCode : SmsStatusResult()
    data class CodeReceived(val code: String, val fullMessage: String) : SmsStatusResult()
    data object Canceled : SmsStatusResult()
    data class Error(val message: String, val rawResponse: String) : SmsStatusResult()
}

class SmsActivationApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
) {
    companion object {
        const val DEFAULT_API_KEY = "ca9b6a6184e346618dde303c12ae1993"
        const val DEFAULT_BASE_URL = "https://api.sms-activate.org/stubs/handler_api.php"

        val AVAILABLE_SERVICES = listOf(
            ServiceTarget("wa", "WhatsApp", "whatsapp", "WhatsApp Messenger & Business OTP"),
            ServiceTarget("tg", "Telegram", "telegram", "Telegram Cloud Auth & 2FA"),
            ServiceTarget("ig", "Instagram", "instagram", "Instagram Verification"),
            ServiceTarget("fb", "Facebook", "facebook", "Facebook Security & Account"),
            ServiceTarget("go", "Google", "google", "Google / Gmail 2FA"),
            ServiceTarget("tw", "Twitter / X", "twitter", "X / Twitter SMS Code"),
            ServiceTarget("lf", "TikTok", "tiktok", "TikTok Mobile Verification"),
            ServiceTarget("dr", "Discord", "discord", "Discord Security SMS")
        )

        val AVAILABLE_COUNTRIES = listOf(
            CountryTarget("187", "United States", "🇺🇸", "+1"),
            CountryTarget("16", "United Kingdom", "🇬🇧", "+44"),
            CountryTarget("22", "India", "🇮🇳", "+91"),
            CountryTarget("36", "Canada", "🇨🇦", "+1"),
            CountryTarget("6", "Indonesia", "🇮🇩", "+62"),
            CountryTarget("4", "Philippines", "🇵🇭", "+63"),
            CountryTarget("73", "Brazil", "🇧🇷", "+55"),
            CountryTarget("43", "Germany", "🇩🇪", "+49"),
            CountryTarget("78", "France", "🇫🇷", "+33"),
            CountryTarget("48", "Netherlands", "🇳🇱", "+31"),
            CountryTarget("174", "Australia", "🇦🇺", "+61")
        )
    }

    /**
     * Action 1: Get Balance / Check API status
     */
    suspend fun getBalance(
        apiKey: String = DEFAULT_API_KEY,
        baseUrl: String = DEFAULT_BASE_URL
    ): Result<Pair<Double, String>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid Base URL: $baseUrl"))

            urlBuilder.addQueryParameter("api_key", apiKey.trim())
            urlBuilder.addQueryParameter("action", "getBalance")

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "RaunakExploits/3.4 (Android; Security-Client)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty().trim()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: $body")
                    )
                }

                if (body.startsWith("ACCESS_BALANCE:")) {
                    val balanceStr = body.substringAfter("ACCESS_BALANCE:").trim()
                    val balance = balanceStr.toDoubleOrNull() ?: 0.0
                    Result.success(Pair(balance, body))
                } else {
                    Result.failure(IOException("API Error: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Action 2: Rent a real number for a specific service and country
     */
    suspend fun getNumber(
        serviceCode: String,
        countryId: String,
        apiKey: String = DEFAULT_API_KEY,
        baseUrl: String = DEFAULT_BASE_URL
    ): Result<RentedNumberResult> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid Base URL: $baseUrl"))

            urlBuilder.addQueryParameter("api_key", apiKey.trim())
            urlBuilder.addQueryParameter("action", "getNumber")
            urlBuilder.addQueryParameter("service", serviceCode)
            urlBuilder.addQueryParameter("country", countryId)

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "RaunakExploits/3.4 (Android; Security-Client)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty().trim()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: $body")
                    )
                }

                // Response format: ACCESS_NUMBER:$id:$number
                if (body.startsWith("ACCESS_NUMBER:")) {
                    val parts = body.split(":")
                    if (parts.size >= 3) {
                        val activationId = parts[1]
                        val rawNumber = parts[2]
                        val formattedNumber = if (!rawNumber.startsWith("+")) "+$rawNumber" else rawNumber
                        Result.success(
                            RentedNumberResult(
                                activationId = activationId,
                                phoneNumber = formattedNumber,
                                serviceCode = serviceCode,
                                countryId = countryId,
                                rawResponse = body
                            )
                        )
                    } else {
                        Result.failure(IOException("Malformed ACCESS_NUMBER response: $body"))
                    }
                } else {
                    Result.failure(IOException("Rent Failed: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Action 3: Poll/Check status for incoming real SMS/OTP on rented ID
     */
    suspend fun getStatus(
        activationId: String,
        apiKey: String = DEFAULT_API_KEY,
        baseUrl: String = DEFAULT_BASE_URL
    ): Result<SmsStatusResult> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid Base URL: $baseUrl"))

            urlBuilder.addQueryParameter("api_key", apiKey.trim())
            urlBuilder.addQueryParameter("action", "getStatus")
            urlBuilder.addQueryParameter("id", activationId)

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "RaunakExploits/3.4 (Android; Security-Client)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty().trim()
                if (!response.isSuccessful) {
                    return@withContext Result.success(
                        SmsStatusResult.Error("HTTP ${response.code}", body)
                    )
                }

                when {
                    body.startsWith("STATUS_OK:") -> {
                        val code = body.substringAfter("STATUS_OK:").trim()
                        val fullMessage = "Verification code: $code. Full SMS received from carrier gateway."
                        Result.success(SmsStatusResult.CodeReceived(code = code, fullMessage = fullMessage))
                    }
                    body.startsWith("STATUS_WAIT_CODE") -> {
                        Result.success(SmsStatusResult.WaitingForCode)
                    }
                    body.startsWith("STATUS_WAIT_RETRY:") -> {
                        val lastCode = body.substringAfter("STATUS_WAIT_RETRY:").trim()
                        Result.success(SmsStatusResult.CodeReceived(code = lastCode, fullMessage = "Last received code: $lastCode (Waiting for new retry)"))
                    }
                    body.startsWith("STATUS_CANCEL") -> {
                        Result.success(SmsStatusResult.Canceled)
                    }
                    else -> {
                        Result.success(SmsStatusResult.Error(body, body))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Action 4: Set activation status (1 = SMS sent, 6 = Finish, 8 = Cancel)
     */
    suspend fun setStatus(
        activationId: String,
        status: Int, // 1 = SMS Sent, 6 = Completed, 8 = Canceled
        apiKey: String = DEFAULT_API_KEY,
        baseUrl: String = DEFAULT_BASE_URL
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid Base URL: $baseUrl"))

            urlBuilder.addQueryParameter("api_key", apiKey.trim())
            urlBuilder.addQueryParameter("action", "setStatus")
            urlBuilder.addQueryParameter("id", activationId)
            urlBuilder.addQueryParameter("status", status.toString())

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "RaunakExploits/3.4 (Android; Security-Client)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty().trim()
                Result.success(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
