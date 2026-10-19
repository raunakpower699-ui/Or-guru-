package com.example.data.repository

import com.example.data.local.SmsOtpMessageEntity
import com.example.data.local.VirtualNumberEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object NumberPoolGenerator {

    data class CountrySpec(
        val countryCode: String,
        val countryName: String,
        val countryFlag: String,
        val carriers: List<String>,
        val prefix: String,
        val numberBuilder: () -> Pair<String, String> // (Formatted, RawDigits)
    )

    private val specs = listOf(
        CountrySpec(
            countryCode = "US",
            countryName = "United States",
            countryFlag = "🇺🇸",
            carriers = listOf("T-Mobile Virtual-SIM", "Verizon Cloud-Node", "AT&T Cyber-Gateway"),
            prefix = "+1",
            numberBuilder = {
                val areaCodes = listOf(202, 312, 415, 212, 646, 512, 702, 305, 206, 917, 347, 404, 617)
                val area = areaCodes.random()
                val part1 = Random.nextInt(200, 899)
                val part2 = Random.nextInt(1000, 9999)
                val formatted = "+1 ($area) $part1-$part2"
                val raw = "1$area$part1$part2"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "GB",
            countryName = "United Kingdom",
            countryFlag = "🇬🇧",
            carriers = listOf("Vodafone Cyber-SIM", "EE Mobile UK Node", "O2 Secure Gate"),
            prefix = "+44",
            numberBuilder = {
                val prefixes = listOf("7700", "7911", "7828", "7400", "7520")
                val p = prefixes.random()
                val tail = Random.nextInt(100000, 999999)
                val formatted = "+44 $p $tail"
                val raw = "44$p$tail"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "IN",
            countryName = "India",
            countryFlag = "🇮🇳",
            carriers = listOf("Jio Cyber-5G", "Airtel Secure-Node", "Vi Cloud-Gateway"),
            prefix = "+91",
            numberBuilder = {
                val start = listOf(98, 97, 88, 79, 91, 80, 96, 70).random()
                val part1 = Random.nextInt(100, 999)
                val part2 = Random.nextInt(10000, 99999)
                val formatted = "+91 $start$part1 $part2"
                val raw = "91$start$part1$part2"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "CA",
            countryName = "Canada",
            countryFlag = "🇨🇦",
            carriers = listOf("Rogers Cyber-Link", "Telus Cloud-SIM", "Bell Virtual-Node"),
            prefix = "+1",
            numberBuilder = {
                val area = listOf(416, 604, 514, 403, 613, 905).random()
                val part1 = Random.nextInt(210, 899)
                val part2 = Random.nextInt(1000, 9999)
                val formatted = "+1 ($area) $part1-$part2"
                val raw = "1$area$part1$part2"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "DE",
            countryName = "Germany",
            countryFlag = "🇩🇪",
            carriers = listOf("Deutsche Telekom Node", "Vodafone DE Cyber", "O2 Telefonica Secure"),
            prefix = "+49",
            numberBuilder = {
                val prefix = listOf("151", "170", "175", "160", "152").random()
                val part1 = Random.nextInt(1000, 9999)
                val part2 = Random.nextInt(1000, 9999)
                val formatted = "+49 $prefix $part1$part2"
                val raw = "49$prefix$part1$part2"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "FR",
            countryName = "France",
            countryFlag = "🇫🇷",
            carriers = listOf("Orange Cyber-SIM", "SFR Cloud-Node", "Free Mobile Secure"),
            prefix = "+33",
            numberBuilder = {
                val prefix = listOf("6", "7").random()
                val b1 = Random.nextInt(10, 99)
                val b2 = Random.nextInt(10, 99)
                val b3 = Random.nextInt(10, 99)
                val b4 = Random.nextInt(10, 99)
                val formatted = "+33 $prefix $b1 $b2 $b3 $b4"
                val raw = "33$prefix$b1$b2$b3$b4"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "NL",
            countryName = "Netherlands",
            countryFlag = "🇳🇱",
            carriers = listOf("KPN Cyber-Port", "Vodafone NL Node", "T-Mobile Secure NL"),
            prefix = "+31",
            numberBuilder = {
                val part = Random.nextInt(10000000, 99999999)
                val formatted = "+31 6 $part"
                val raw = "316$part"
                Pair(formatted, raw)
            }
        ),
        CountrySpec(
            countryCode = "AU",
            countryName = "Australia",
            countryFlag = "🇦🇺",
            carriers = listOf("Telstra Cyber-SIM", "Optus Cloud-Node", "Vodafone AU Gate"),
            prefix = "+61",
            numberBuilder = {
                val p = listOf("412", "423", "450", "488").random()
                val part1 = Random.nextInt(100, 999)
                val part2 = Random.nextInt(100, 999)
                val formatted = "+61 $p $part1 $part2"
                val raw = "61$p$part1$part2"
                Pair(formatted, raw)
            }
        )
    )

    fun getSupportedCountries(): List<Pair<String, String>> {
        return specs.map { Pair(it.countryCode, "${it.countryFlag} ${it.countryName}") }
    }

    /**
     * Generates a candidate number guaranteed not in blacklistedRawDigits or existingInDb
     */
    fun generateFreshCandidate(
        blacklistedRawDigits: Set<String>,
        existingRawDigits: Set<String>,
        preferredCountryCode: String? = null
    ): VirtualNumberEntity {
        val filteredSpecs = if (preferredCountryCode != null) {
            specs.filter { it.countryCode == preferredCountryCode }.ifEmpty { specs }
        } else {
            specs
        }

        var attempts = 0
        while (attempts < 1000) {
            val spec = filteredSpecs.random()
            val (formatted, raw) = spec.numberBuilder()
            if (!blacklistedRawDigits.contains(raw) && !existingRawDigits.contains(raw)) {
                return VirtualNumberEntity(
                    phoneNumber = formatted,
                    rawDigits = raw,
                    countryCode = spec.countryCode,
                    countryName = spec.countryName,
                    countryFlag = spec.countryFlag,
                    carrier = spec.carriers.random(),
                    status = "ACTIVE",
                    assignedTimestamp = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
                    signalStrength = Random.nextInt(92, 100),
                    smsCount = 0
                )
            }
            attempts++
        }

        // Fallback unique generator with timestamp suffix
        val fallbackSpec = specs.random()
        val timeUnique = (System.currentTimeMillis() % 1000000).toString()
        val raw = "${fallbackSpec.prefix.replace("+", "")}99$timeUnique"
        val formatted = "${fallbackSpec.prefix} 99$timeUnique"
        return VirtualNumberEntity(
            phoneNumber = formatted,
            rawDigits = raw,
            countryCode = fallbackSpec.countryCode,
            countryName = fallbackSpec.countryName,
            countryFlag = fallbackSpec.countryFlag,
            carrier = fallbackSpec.carriers.random(),
            status = "ACTIVE",
            assignedTimestamp = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
            signalStrength = 96,
            smsCount = 0
        )
    }

    data class ServiceOtpTemplate(
        val serviceName: String,
        val senderTag: String,
        val bodyGenerator: (String) -> String
    )

    private val templates = listOf(
        ServiceOtpTemplate(
            serviceName = "WhatsApp",
            senderTag = "WHATSAPP-AUTH",
            bodyGenerator = { otp ->
                val formatted = "${otp.take(3)}-${otp.takeLast(3)}"
                "Your WhatsApp code is $formatted. You can also tap this link to verify your phone: v.whatsapp.com/$otp. Do not share this code."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Telegram",
            senderTag = "TELEGRAM-MSG",
            bodyGenerator = { otp ->
                "Telegram code: $otp. You can also tap here to log in. Do not give this code to anyone, even if they say they're from Telegram!"
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Google",
            senderTag = "GOOGLE-VERIFY",
            bodyGenerator = { otp ->
                "G-$otp is your Google verification code. Never share your security code with anyone."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Instagram",
            senderTag = "INSTAGRAM",
            bodyGenerator = { otp ->
                "$otp is your Instagram verification code. Don't share it with anyone."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Discord",
            senderTag = "DISCORD-SEC",
            bodyGenerator = { otp ->
                "Your Discord security verification code is: $otp. It will expire in 10 minutes."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Steam",
            senderTag = "STEAM-GUARD",
            bodyGenerator = { otp ->
                "Steam Guard code: $otp. Use this code to authorize login on your device."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "OpenAI",
            senderTag = "OPENAI-AUTH",
            bodyGenerator = { otp ->
                "Your OpenAI verification code is $otp. It will expire in 5 minutes."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Netflix",
            senderTag = "NETFLIX-CODE",
            bodyGenerator = { otp ->
                "Netflix: Your temporary sign-in code is $otp. Do not share this code with anyone."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Twitter / X",
            senderTag = "TWITTER-SEC",
            bodyGenerator = { otp ->
                "Your X confirmation code is $otp. Confirm your account verification now."
            }
        ),
        ServiceOtpTemplate(
            serviceName = "Amazon",
            senderTag = "AMAZON-OTP",
            bodyGenerator = { otp ->
                "$otp is your Amazon OTP. Do not share it with anyone. Amazon representatives will never ask for this code."
            }
        )
    )

    fun generateOtpForService(
        phoneNumber: String,
        serviceName: String? = null,
        customCode: String? = null
    ): SmsOtpMessageEntity {
        val template = if (serviceName != null) {
            templates.firstOrNull { it.serviceName.equals(serviceName, ignoreCase = true) } ?: templates.random()
        } else {
            templates.random()
        }

        val code = customCode?.ifBlank { null } ?: String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val body = template.bodyGenerator(code)

        return SmsOtpMessageEntity(
            phoneNumber = phoneNumber,
            sender = template.senderTag,
            serviceName = template.serviceName,
            otpCode = code,
            messageBody = body,
            receivedTimestamp = System.currentTimeMillis(),
            isRead = false,
            isVerified = true
        )
    }

    fun generateInitialOtpsForNumber(phoneNumber: String, count: Int = 2): List<SmsOtpMessageEntity> {
        val selectedTemplates = templates.shuffled().take(count)
        val now = System.currentTimeMillis()
        return selectedTemplates.mapIndexed { index, t ->
            val code = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
            // Stagger timestamps slightly into past (1m to 25m ago)
            val timeOffset = (index + 1) * Random.nextLong(60_000, 600_000)
            SmsOtpMessageEntity(
                phoneNumber = phoneNumber,
                sender = t.senderTag,
                serviceName = t.serviceName,
                otpCode = code,
                messageBody = t.bodyGenerator(code),
                receivedTimestamp = now - timeOffset,
                isRead = false,
                isVerified = true
            )
        }
    }
}
