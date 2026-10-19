package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.SmsActivationApi
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberNeonButton
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun ApiConfigDialog(
    currentApiKey: String,
    currentBaseUrl: String,
    balance: Double?,
    isCheckingBalance: Boolean,
    onSaveConfig: (newKey: String, newUrl: String) -> Unit,
    onTestConnection: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var baseUrlInput by remember { mutableStateOf(currentBaseUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("api_config_dialog"),
            shape = RoundedCornerShape(12.dp),
            color = CyberSurface,
            border = BorderStroke(1.dp, CyberPrimary)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
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
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = CyberPrimary
                        )
                        Text(
                            text = "SMS ACTIVATION API CONFIG",
                            color = CyberPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Balance status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(
                        text = if (balance != null) "STATUS: ONLINE ($$balance)" else "STATUS: UNVERIFIED",
                        color = if (balance != null) CyberSecondary else CyberPrimary,
                        hasDot = true
                    )

                    CyberNeonButton(
                        text = if (isCheckingBalance) "CHECKING..." else "PING STATUS",
                        icon = Icons.Default.Refresh,
                        onClick = onTestConnection,
                        accentColor = CyberSecondary,
                        testTag = "ping_balance_button"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "API KEY (SMS-ACTIVATE / 5SIM):",
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPrimary,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "GATEWAY ENDPOINT URL:",
                    color = CyberTextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = baseUrlInput,
                    onValueChange = { baseUrlInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPrimary,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        focusedContainerColor = CyberSurfaceVariant,
                        unfocusedContainerColor = CyberSurfaceVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Default reset button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberNeonButton(
                        text = "RESET DEFAULT",
                        onClick = {
                            apiKeyInput = SmsActivationApi.DEFAULT_API_KEY
                            baseUrlInput = SmsActivationApi.DEFAULT_BASE_URL
                        },
                        modifier = Modifier.weight(1f),
                        accentColor = CyberTextMuted,
                        testTag = "reset_api_defaults"
                    )

                    CyberNeonButton(
                        text = "SAVE & SYNC",
                        icon = Icons.Default.Save,
                        onClick = {
                            onSaveConfig(apiKeyInput, baseUrlInput)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        accentColor = CyberPrimary,
                        testTag = "save_api_config"
                    )
                }
            }
        }
    }
}
