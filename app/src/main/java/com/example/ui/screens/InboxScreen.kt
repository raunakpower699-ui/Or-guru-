package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SmsOtpMessageEntity
import com.example.data.local.VirtualNumberEntity
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberCountdownTimer
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.InstantCopyButton
import com.example.ui.components.PulsingStatusDot
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTertiary
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    number: VirtualNumberEntity,
    messages: List<SmsOtpMessageEntity>,
    isPollingActive: Boolean,
    pollingAttempt: Int,
    onBack: () -> Unit,
    onQuickSendOtp: (serviceName: String) -> Unit,
    onOpenSimulateDialog: () -> Unit,
    onTogglePolling: () -> Unit,
    onClearHistory: () -> Unit,
    onCancelRental: () -> Unit,
    onFinishRental: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val isRealApi = number.isRealApiRental && number.activationId != null
    val accentColor = if (isRealApi) CyberSecondary else CyberPrimary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isRealApi) "REAL API ACTIVATION INBOX" else "LIVE SMS // OTP INBOX",
                                style = MaterialTheme.typography.titleMedium,
                                color = accentColor
                            )
                            PulsingStatusDot(color = CyberSecondary)
                        }
                        Text(
                            text = if (isRealApi) "ACTIVATION ID: #${number.activationId} | POLLING: $pollingAttempt" else "PORT: 443 | GSM ENCRYPTED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = CyberTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("inbox_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = accentColor
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = CyberTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBackground
                )
            )
        },
        containerColor = CyberBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Selected Number Information Card
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("number_detail_card"),
                borderColor = accentColor.copy(alpha = 0.6f),
                glowColor = accentColor
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = number.countryFlag, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = number.countryName.uppercase(),
                                    color = CyberTextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = number.carrier,
                                    color = accentColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        CyberBadge(
                            text = if (isRealApi) "REAL API NODE" else "ONLINE",
                            color = CyberSecondary,
                            hasDot = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Number with Instant Copy Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRealApi) "RENTED REAL NUMBER" else "VIRTUAL NUMBER",
                                color = CyberTextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Text(
                                text = number.phoneNumber,
                                color = CyberTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        InstantCopyButton(
                            textToCopy = number.phoneNumber,
                            label = "COPY",
                            accentColor = accentColor,
                            testTag = "copy_phone_number_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CyberCountdownTimer(
                            targetTimestamp = number.expiresAt
                        )

                        Text(
                            text = if (isRealApi) "STATUS: ${number.rentalStatus}" else "SIGNAL: ${number.signalStrength}%",
                            color = if (number.rentalStatus == "CODE_RECEIVED") CyberSecondary else CyberTertiary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // For real API rentals: Action Buttons to Finish or Cancel
                    if (isRealApi) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberNeonButton(
                                text = "COMPLETE ACTIVATION",
                                icon = Icons.Default.DoneAll,
                                onClick = onFinishRental,
                                modifier = Modifier.weight(1f),
                                accentColor = CyberSecondary,
                                testTag = "finish_activation_btn"
                            )

                            CyberNeonButton(
                                text = "CANCEL ACTIVATION",
                                icon = Icons.Default.Cancel,
                                onClick = onCancelRental,
                                modifier = Modifier.weight(1f),
                                accentColor = CyberError,
                                testTag = "cancel_activation_btn"
                            )
                        }
                    }
                }
            }

            // Radar Polling Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPollingActive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = CyberSecondary,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        text = if (isRealApi) "POLLING SERVER FOR REAL OTP (ATTEMPT #$pollingAttempt)" else "SIMULATED / TEST OTP INJECTOR",
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isPollingActive) "AUTO-POLL: ON" else "AUTO-POLL: OFF",
                        color = if (isPollingActive) CyberSecondary else CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Switch(
                        checked = isPollingActive,
                        onCheckedChange = { onTogglePolling() },
                        modifier = Modifier.testTag("radar_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberSecondary,
                            checkedTrackColor = CyberSecondary.copy(alpha = 0.3f),
                            uncheckedThumbColor = CyberTextMuted,
                            uncheckedTrackColor = CyberSurfaceVariant
                        )
                    )
                }
            }

            // Quick triggers (if testing or simulated)
            if (!isRealApi) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("WhatsApp", "Telegram", "Instagram", "Facebook", "Google").forEach { service ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .testTag("trigger_${service.lowercase()}")
                                .background(CyberSurfaceVariant),
                            color = CyberSurfaceVariant,
                            border = BorderStroke(1.dp, CyberBorder),
                            shape = RoundedCornerShape(6.dp),
                            onClick = { onQuickSendOtp(service) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = CyberSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = service,
                                    color = CyberTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .testTag("trigger_custom_otp")
                            .background(CyberPrimary.copy(alpha = 0.15f)),
                        color = CyberPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyberPrimary),
                        shape = RoundedCornerShape(6.dp),
                        onClick = onOpenSimulateDialog
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Custom...",
                                color = CyberPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SMS Inbound Stream Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RECEIVED MESSAGES (${messages.size})",
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = if (isRealApi) "STATUS: LIVE API" else "STATUS: ACTIVE",
                    color = CyberSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SMS Messages List
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar Listening",
                            tint = accentColor,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (isRealApi) "WAITING FOR REAL SMS ACTIVATION..." else "LISTENING FOR GSM TRANSMISSIONS...",
                            color = accentColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (isRealApi)
                                "Copy the number above and request an OTP on your target app. As soon as the carrier transmits the SMS, it will appear here instantly!"
                            else
                                "Use this number on WhatsApp, Telegram, or Google. Or tap any button above to simulate immediate incoming OTP.",
                            color = CyberTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sms_messages_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        SmsOtpCard(message = msg)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun SmsOtpCard(
    message: SmsOtpMessageEntity
) {
    val serviceColor = when (message.serviceName.lowercase()) {
        "whatsapp" -> CyberSecondary
        "telegram" -> CyberPrimary
        "google" -> CyberTertiary
        "steam" -> Color(0xFF66C0F4)
        "discord" -> Color(0xFF5865F2)
        "netflix" -> CyberError
        "instagram" -> Color(0xFFE1306C)
        "facebook" -> Color(0xFF1877F2)
        else -> CyberPrimary
    }

    val timeFormatted = remember(message.receivedTimestamp) {
        val diff = System.currentTimeMillis() - message.receivedTimestamp
        val minutes = diff / (1000 * 60)
        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            else -> SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(message.receivedTimestamp))
        }
    }

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sms_card_${message.id}"),
        borderColor = serviceColor.copy(alpha = 0.4f),
        glowColor = serviceColor
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Sender tag, Service badge, time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberBadge(
                        text = message.serviceName.uppercase(),
                        color = serviceColor,
                        hasDot = true
                    )
                    Text(
                        text = message.sender,
                        color = CyberTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = timeFormatted,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted OTP Box with 1-Tap Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceElevated)
                    .border(1.dp, serviceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ONE-TIME CODE",
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = message.otpCode,
                        color = serviceColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 2.sp
                    )
                }

                InstantCopyButton(
                    textToCopy = message.otpCode,
                    label = "COPY OTP",
                    accentColor = serviceColor,
                    testTag = "copy_otp_${message.otpCode}"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full message text
            Text(
                text = message.messageBody,
                color = CyberTextPrimary,
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Verification status footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyberSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "REAL CARRIER TRANSMISSION // VERIFIED",
                    color = CyberSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

