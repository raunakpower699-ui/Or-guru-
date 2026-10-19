package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.SmsActivationApi
import com.example.data.local.AppDatabase
import com.example.data.repository.NumberRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: NumberRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NumberRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Raunak Exploits", appName)
    }

    @Test
    fun `default API key matches user configuration`() {
        assertEquals("ca9b6a6184e346618dde303c12ae1993", SmsActivationApi.DEFAULT_API_KEY)
    }

    @Test
    fun `anti-loop engine provisions exactly 10 fresh numbers with zero repetition`() = runBlocking {
        // First allocation
        val count = repository.ensureDailyQuota(forceRefresh = false)
        assertEquals(10, count)

        val firstRound = repository.activeNumbers.first()
        assertEquals(10, firstRound.size)
        val firstRawDigits = firstRound.map { it.rawDigits }.toSet()
        assertEquals(10, firstRawDigits.size) // All distinct

        // Force 24h refresh: previous 10 must be blacklisted and replaced with 10 completely fresh numbers
        repository.ensureDailyQuota(forceRefresh = true)

        val secondRound = repository.activeNumbers.first()
        assertEquals(10, secondRound.size)
        val secondRawDigits = secondRound.map { it.rawDigits }.toSet()

        // Zero repetition assertion: no number from round 1 should appear in round 2
        val overlap = firstRawDigits.intersect(secondRawDigits)
        assertTrue("Expected zero repetition between consecutive cycles, found: $overlap", overlap.isEmpty())
    }

    @Test
    fun `injecting incoming OTP creates verified message`() = runBlocking {
        repository.ensureDailyQuota(forceRefresh = false)
        val numbers = repository.activeNumbers.first()
        val target = numbers.first()

        val msg = repository.injectIncomingOtp(
            phoneNumber = target.phoneNumber,
            serviceName = "WhatsApp",
            customCode = "778899"
        )

        assertEquals("778899", msg.otpCode)
        assertEquals("WhatsApp", msg.serviceName)

        val messages = repository.getMessagesForNumber(target.phoneNumber).first()
        assertTrue(messages.any { it.otpCode == "778899" })
    }
}
