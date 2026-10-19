package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ApiConfigDialog
import com.example.ui.screens.AuditLogDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.SimulateOtpDialog
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as RaunakApp
        MainViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBackground
                ) {
                    RaunakAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RaunakAppContent(viewModel: MainViewModel) {
    val activeNumbers by viewModel.filteredNumbers.collectAsStateWithLifecycle()
    val rawNumbers by viewModel.rawActiveNumbers.collectAsStateWithLifecycle()
    val selectedNumber by viewModel.selectedNumber.collectAsStateWithLifecycle()
    val messages by viewModel.messagesForSelected.collectAsStateWithLifecycle()
    val selectedCountryFilter by viewModel.selectedCountryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val cycleExpiryTimestamp by viewModel.cycleExpiryTimestamp.collectAsStateWithLifecycle()
    val blacklistedCount by viewModel.activeBlacklistCount.collectAsStateWithLifecycle()
    val isPollingActive by viewModel.isPollingActive.collectAsStateWithLifecycle()
    val showAuditDialog by viewModel.showAuditDialog.collectAsStateWithLifecycle()
    val showSimulateDialog by viewModel.showSimulateDialog.collectAsStateWithLifecycle()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    // Real API states
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val baseUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
    val apiBalance by viewModel.apiBalance.collectAsStateWithLifecycle()
    val isCheckingBalance by viewModel.isCheckingBalance.collectAsStateWithLifecycle()
    val selectedServiceCode by viewModel.selectedServiceCode.collectAsStateWithLifecycle()
    val selectedRentCountryId by viewModel.selectedRentCountryId.collectAsStateWithLifecycle()
    val isRentingNumber by viewModel.isRentingNumber.collectAsStateWithLifecycle()
    val lastApiError by viewModel.lastApiError.collectAsStateWithLifecycle()
    val pollingAttempt by viewModel.pollingAttempt.collectAsStateWithLifecycle()

    val currentSelected = selectedNumber
    if (currentSelected != null) {
        InboxScreen(
            number = currentSelected,
            messages = messages,
            isPollingActive = isPollingActive,
            pollingAttempt = pollingAttempt,
            onBack = { viewModel.clearSelectedNumber() },
            onQuickSendOtp = { service -> viewModel.sendTestOtp(serviceName = service) },
            onOpenSimulateDialog = { viewModel.toggleSimulateDialog(true) },
            onTogglePolling = { viewModel.togglePolling() },
            onClearHistory = { viewModel.clearHistory() },
            onCancelRental = { viewModel.cancelActiveRental() },
            onFinishRental = { viewModel.finishActiveRental() }
        )
    } else {
        HomeScreen(
            activeNumbers = activeNumbers,
            totalActiveCount = rawNumbers.size,
            blacklistedCount = blacklistedCount,
            cycleExpiryTimestamp = cycleExpiryTimestamp,
            selectedCountryFilter = selectedCountryFilter,
            searchQuery = searchQuery,
            isRefreshing = isRefreshing,
            apiBalance = apiBalance,
            isCheckingBalance = isCheckingBalance,
            selectedServiceCode = selectedServiceCode,
            selectedRentCountryId = selectedRentCountryId,
            isRentingNumber = isRentingNumber,
            lastApiError = lastApiError,
            onSelectNumber = { viewModel.selectNumber(it) },
            onCountryFilterSelected = { viewModel.setCountryFilter(it) },
            onSearchChanged = { viewModel.setSearchQuery(it) },
            onForceRefresh = { viewModel.forceRefreshDailyCycle() },
            onOpenAuditLogs = { viewModel.toggleAuditDialog(true) },
            onOpenApiConfig = { viewModel.toggleApiKeyDialog(true) },
            onCheckBalance = { viewModel.checkBalance() },
            onSelectRentService = { viewModel.setSelectedService(it) },
            onSelectRentCountry = { viewModel.setSelectedCountry(it) },
            onRentNumber = { viewModel.rentRealNumber() },
            onDismissApiError = { viewModel.clearApiError() }
        )
    }

    if (showAuditDialog) {
        AuditLogDialog(
            logs = auditLogs,
            blacklistedCount = blacklistedCount,
            onDismiss = { viewModel.toggleAuditDialog(false) }
        )
    }

    if (showApiKeyDialog) {
        ApiConfigDialog(
            currentApiKey = apiKey,
            currentBaseUrl = baseUrl,
            balance = apiBalance,
            isCheckingBalance = isCheckingBalance,
            onSaveConfig = { newKey, newUrl ->
                viewModel.setApiKey(newKey)
                viewModel.setBaseUrl(newUrl)
            },
            onTestConnection = { viewModel.checkBalance() },
            onDismiss = { viewModel.toggleApiKeyDialog(false) }
        )
    }

    if (showSimulateDialog && currentSelected != null) {
        SimulateOtpDialog(
            phoneNumber = currentSelected.phoneNumber,
            onSendOtp = { service, code ->
                viewModel.sendTestOtp(serviceName = service, customCode = code)
            },
            onDismiss = { viewModel.toggleSimulateDialog(false) }
        )
    }
}
