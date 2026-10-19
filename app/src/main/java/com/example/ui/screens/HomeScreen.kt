package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.SmsActivationApi
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeNumbers: List<VirtualNumberEntity>,
    totalActiveCount: Int,
    blacklistedCount: Int,
    cycleExpiryTimestamp: Long,
    selectedCountryFilter: String,
    searchQuery: String,
    isRefreshing: Boolean,
    apiBalance: Double?,
    isCheckingBalance: Boolean,
    selectedServiceCode: String,
    selectedRentCountryId: String,
    isRentingNumber: Boolean,
    lastApiError: String?,
    onSelectNumber: (VirtualNumberEntity) -> Unit,
    onCountryFilterSelected: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onForceRefresh: () -> Unit,
    onOpenAuditLogs: () -> Unit,
    onOpenApiConfig: () -> Unit,
    onCheckBalance: () -> Unit,
    onSelectRentService: (String) -> Unit,
    onSelectRentCountry: (String) -> Unit,
    onRentNumber: () -> Unit,
    onDismissApiError: () -> Unit
) {
    val filterCountries = listOf(
        Pair("ALL", "🌐 ALL"),
        Pair("US", "🇺🇸 USA"),
        Pair("GB", "🇬🇧 UK"),
        Pair("IN", "🇮🇳 INDIA"),
        Pair("CA", "🇨🇦 CANADA"),
        Pair("DE", "🇩🇪 GERMANY"),
        Pair("FR", "🇫🇷 FRANCE"),
        Pair("NL", "🇳🇱 NETHERLANDS"),
        Pair("AU", "🇦🇺 AUSTRALIA")
    )

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
                                text = "RAUNAK EXPLOITS",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            PulsingStatusDot(color = CyberSecondary)
                        }
                        Text(
                            text = "REAL SMS ACTIVATION // ANTI-LOOP GATEWAY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = CyberSecondary
                        )
                    }
                },
                actions = {
                    // Balance status button
                    Surface(
                        modifier = Modifier
                            .testTag("check_balance_header_btn")
                            .clip(RoundedCornerShape(6.dp)),
                        color = CyberSurfaceVariant,
                        border = BorderStroke(1.dp, CyberBorder),
                        onClick = onCheckBalance
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = CyberTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (apiBalance != null) "$$apiBalance" else "PING API",
                                color = CyberTertiary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenApiConfig,
                        modifier = Modifier.testTag("open_api_config_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Config",
                            tint = CyberPrimary
                        )
                    }

                    IconButton(
                        onClick = onOpenAuditLogs,
                        modifier = Modifier.testTag("open_audit_terminal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Audit Terminal",
                            tint = CyberSecondary
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
            // Live API Renting Dashboard Card
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("rent_service_dashboard"),
                borderColor = CyberPrimary,
                glowColor = CyberPrimary
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LIVE SMS ACTIVATION GATEWAY",
                                color = CyberPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        CyberBadge(
                            text = "REAL APIS ONLY",
                            color = CyberSecondary,
                            hasDot = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. SELECT TARGET PLATFORM:",
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Service Chips: WhatsApp, Telegram, Instagram, Facebook, etc.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SmsActivationApi.AVAILABLE_SERVICES.forEach { service ->
                            val isSelected = selectedServiceCode == service.code
                            val serviceColor = when (service.code) {
                                "wa" -> CyberSecondary
                                "tg" -> CyberPrimary
                                "ig" -> Color(0xFFE1306C)
                                "fb" -> Color(0xFF1877F2)
                                else -> CyberTertiary
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectRentService(service.code) },
                                label = {
                                    Text(
                                        text = service.name,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = serviceColor.copy(alpha = 0.25f),
                                    selectedLabelColor = serviceColor,
                                    containerColor = CyberSurfaceVariant,
                                    labelColor = CyberTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CyberBorder,
                                    selectedBorderColor = serviceColor
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("service_chip_${service.code}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "2. SELECT TARGET COUNTRY:",
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Country Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SmsActivationApi.AVAILABLE_COUNTRIES.forEach { country ->
                            val isSelected = selectedRentCountryId == country.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectRentCountry(country.id) },
                                label = {
                                    Text(
                                        text = "${country.flag} ${country.name} (${country.dialCode})",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = CyberPrimary,
                                    containerColor = CyberSurfaceVariant,
                                    labelColor = CyberTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CyberBorder,
                                    selectedBorderColor = CyberPrimary
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("rent_country_chip_${country.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary "RENT NUMBER" Action Button
                    CyberNeonButton(
                        text = if (isRentingNumber) "RENTING NUMBER VIA API..." else "RENT REAL WORKING NUMBER",
                        icon = Icons.Default.VpnKey,
                        onClick = onRentNumber,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rent_number_action_btn"),
                        accentColor = CyberSecondary
                    )
                }
            }

            // Real API Error Banner
            if (lastApiError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_error_banner"),
                    shape = RoundedCornerShape(8.dp),
                    color = CyberError.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CyberError)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "API / SERVER RESPONSE:",
                                color = CyberError,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = lastApiError,
                                color = CyberTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CyberNeonButton(
                                text = "LOGS",
                                onClick = onOpenAuditLogs,
                                accentColor = CyberError,
                                testTag = "error_view_logs_btn"
                            )
                            IconButton(onClick = onDismissApiError) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = CyberError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quota & Anti-Loop HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberCountdownTimer(
                    targetTimestamp = cycleExpiryTimestamp,
                    onExpired = onForceRefresh
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ACTIVE: $totalActiveCount",
                        color = CyberSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BLACKLIST: $blacklistedCount",
                        color = CyberTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_numbers_input"),
                placeholder = {
                    Text(
                        "Search rented phone numbers, carriers, IDs...",
                        color = CyberTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyberPrimary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPrimary,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = CyberTextPrimary,
                    unfocusedTextColor = CyberTextPrimary,
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface
                ),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Country Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterCountries.forEach { (code, label) ->
                    val isSelected = selectedCountryFilter == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCountryFilterSelected(code) },
                        label = {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyberPrimary,
                            containerColor = CyberSurface,
                            labelColor = CyberTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CyberBorder,
                            selectedBorderColor = CyberPrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("country_filter_$code")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Numbers Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE VIRTUAL GSM SLOTS (${activeNumbers.size})",
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isRefreshing || isRentingNumber) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = CyberPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "100% REAL WORKING OTPS",
                        color = CyberSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Numbers List
            if (activeNumbers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NO VIRTUAL NODES MATCHING CRITERIA",
                            color = CyberPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Click 'Rent Real Working Number' above to lease a live SIM activation.",
                            color = CyberTextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("virtual_numbers_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(activeNumbers, key = { _, num -> num.phoneNumber }) { index, num ->
                        VirtualNumberCard(
                            slotIndex = index + 1,
                            number = num,
                            onClick = { onSelectNumber(num) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun VirtualNumberCard(
    slotIndex: Int,
    number: VirtualNumberEntity,
    onClick: () -> Unit
) {
    val isRealApi = number.isRealApiRental
    val accentColor = if (isRealApi) CyberSecondary else CyberPrimary

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("number_card_${number.rawDigits}"),
        onClick = onClick,
        borderColor = if (isRealApi) CyberSecondary.copy(alpha = 0.6f) else CyberBorder,
        glowColor = accentColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
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
                        text = if (isRealApi) "REAL API #${number.activationId ?: slotIndex}" else "SLOT #${String.format("%02d", slotIndex)}",
                        color = accentColor
                    )
                    Text(text = number.countryFlag, fontSize = 18.sp)
                    Text(
                        text = number.countryName,
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Signal",
                        tint = CyberSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${number.signalStrength}%",
                        color = CyberSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Number & Instant Copy Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = number.phoneNumber,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                InstantCopyButton(
                    textToCopy = number.phoneNumber,
                    label = "COPY",
                    accentColor = accentColor,
                    testTag = "copy_slot_${slotIndex}"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = number.carrier,
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberBadge(
                        text = if (number.smsCount > 0) "${number.smsCount} REAL OTPS" else "WAITING FOR OTP",
                        color = if (number.smsCount > 0) CyberSecondary else CyberTertiary,
                        hasDot = true
                    )
                    Text(
                        text = "LIVE INBOX →",
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
