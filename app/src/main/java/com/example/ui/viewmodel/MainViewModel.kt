package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.SmsActivationApi
import com.example.data.api.SmsStatusResult
import com.example.data.local.SmsOtpMessageEntity
import com.example.data.local.VirtualNumberEntity
import com.example.data.repository.NumberRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(private val repository: NumberRepository) : ViewModel() {

    // API Configuration
    private val _apiKey = MutableStateFlow(SmsActivationApi.DEFAULT_API_KEY)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _baseUrl = MutableStateFlow(SmsActivationApi.DEFAULT_BASE_URL)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _apiBalance = MutableStateFlow<Double?>(null)
    val apiBalance: StateFlow<Double?> = _apiBalance.asStateFlow()

    private val _isCheckingBalance = MutableStateFlow(false)
    val isCheckingBalance: StateFlow<Boolean> = _isCheckingBalance.asStateFlow()

    // Service & Country selection for renting
    private val _selectedServiceCode = MutableStateFlow("wa") // WhatsApp default
    val selectedServiceCode: StateFlow<String> = _selectedServiceCode.asStateFlow()

    private val _selectedRentCountryId = MutableStateFlow("187") // USA default
    val selectedRentCountryId: StateFlow<String> = _selectedRentCountryId.asStateFlow()

    private val _isRentingNumber = MutableStateFlow(false)
    val isRentingNumber: StateFlow<Boolean> = _isRentingNumber.asStateFlow()

    private val _lastApiError = MutableStateFlow<String?>(null)
    val lastApiError: StateFlow<String?> = _lastApiError.asStateFlow()

    private val _pollingAttempt = MutableStateFlow(0)
    val pollingAttempt: StateFlow<Int> = _pollingAttempt.asStateFlow()

    // Navigation and numbers
    private val _selectedNumber = MutableStateFlow<VirtualNumberEntity?>(null)
    val selectedNumber: StateFlow<VirtualNumberEntity?> = _selectedNumber.asStateFlow()

    private val _selectedCountryFilter = MutableStateFlow("ALL")
    val selectedCountryFilter: StateFlow<String> = _selectedCountryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _cycleExpiryTimestamp = MutableStateFlow(System.currentTimeMillis() + 24 * 3600 * 1000L)
    val cycleExpiryTimestamp: StateFlow<Long> = _cycleExpiryTimestamp.asStateFlow()

    private val _activeBlacklistCount = MutableStateFlow(0)
    val activeBlacklistCount: StateFlow<Int> = _activeBlacklistCount.asStateFlow()

    private val _isPollingActive = MutableStateFlow(true)
    val isPollingActive: StateFlow<Boolean> = _isPollingActive.asStateFlow()

    private val _showAuditDialog = MutableStateFlow(false)
    val showAuditDialog: StateFlow<Boolean> = _showAuditDialog.asStateFlow()

    private val _showSimulateDialog = MutableStateFlow(false)
    val showSimulateDialog: StateFlow<Boolean> = _showSimulateDialog.asStateFlow()

    private val _showApiKeyDialog = MutableStateFlow(false)
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private var realPollingJob: Job? = null
    private var simulatedPollingJob: Job? = null
    private var messagesJob: Job? = null

    val rawActiveNumbers: StateFlow<List<VirtualNumberEntity>> = repository.activeNumbers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val auditLogs: StateFlow<List<String>> = repository.auditLogs

    val filteredNumbers: StateFlow<List<VirtualNumberEntity>> = combine(
        rawActiveNumbers,
        _selectedCountryFilter,
        _searchQuery
    ) { numbers, country, query ->
        numbers.filter { number ->
            val matchesCountry = country == "ALL" || number.countryCode.equals(country, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    number.phoneNumber.contains(query, ignoreCase = true) ||
                    number.countryName.contains(query, ignoreCase = true) ||
                    number.carrier.contains(query, ignoreCase = true) ||
                    (number.activationId != null && number.activationId.contains(query, ignoreCase = true))
            matchesCountry && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _messagesForSelected = MutableStateFlow<List<SmsOtpMessageEntity>>(emptyList())
    val messagesForSelected: StateFlow<List<SmsOtpMessageEntity>> = _messagesForSelected.asStateFlow()

    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.ensureDailyQuota(forceRefresh = false)
            updateMeta()
            _isRefreshing.value = false
            // Check balance automatically on launch
            checkBalance()
        }
        startSimulatedPolling()
    }

    private suspend fun updateMeta() {
        _cycleExpiryTimestamp.value = repository.getCycleExpiryTimestamp()
        _activeBlacklistCount.value = repository.getActiveBlacklistCount()
    }

    // =========================================================================
    // Real SMS Activation API Operations
    // =========================================================================

    fun checkBalance() {
        viewModelScope.launch {
            _isCheckingBalance.value = true
            _lastApiError.value = null
            val result = repository.checkApiBalance(_apiKey.value, _baseUrl.value)
            result.onSuccess { (bal, _) ->
                _apiBalance.value = bal
            }.onFailure { err ->
                _lastApiError.value = err.message
            }
            _isCheckingBalance.value = false
        }
    }

    fun rentRealNumber() {
        viewModelScope.launch {
            _isRentingNumber.value = true
            _lastApiError.value = null
            val result = repository.rentRealNumber(
                serviceCode = _selectedServiceCode.value,
                countryId = _selectedRentCountryId.value,
                apiKey = _apiKey.value,
                baseUrl = _baseUrl.value
            )

            result.onSuccess { rentedEntity ->
                _selectedNumber.value = rentedEntity
                selectNumber(rentedEntity)
                // Refresh balance after renting
                checkBalance()
            }.onFailure { err ->
                _lastApiError.value = err.message
            }
            _isRentingNumber.value = false
        }
    }

    fun cancelActiveRental() {
        val current = _selectedNumber.value ?: return
        val id = current.activationId ?: return
        viewModelScope.launch {
            repository.cancelRealActivation(id, _apiKey.value, _baseUrl.value)
            realPollingJob?.cancel()
            _selectedNumber.value = current.copy(rentalStatus = "CANCELED")
        }
    }

    fun finishActiveRental() {
        val current = _selectedNumber.value ?: return
        val id = current.activationId ?: return
        viewModelScope.launch {
            repository.finishRealActivation(id, _apiKey.value, _baseUrl.value)
            realPollingJob?.cancel()
            _selectedNumber.value = current.copy(rentalStatus = "FINISHED")
        }
    }

    fun setApiKey(newKey: String) {
        _apiKey.value = newKey.trim()
        checkBalance()
    }

    fun setBaseUrl(newUrl: String) {
        _baseUrl.value = newUrl.trim()
        checkBalance()
    }

    fun setSelectedService(serviceCode: String) {
        _selectedServiceCode.value = serviceCode
    }

    fun setSelectedCountry(countryId: String) {
        _selectedRentCountryId.value = countryId
    }

    fun clearApiError() {
        _lastApiError.value = null
    }

    // =========================================================================
    // Number selection & Live Polling
    // =========================================================================

    fun selectNumber(number: VirtualNumberEntity) {
        _selectedNumber.value = number
        _pollingAttempt.value = 0
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForNumber(number.phoneNumber).collect { msgs ->
                _messagesForSelected.value = msgs
            }
        }

        // If this is a real rented number from the API, start active live polling for real OTP
        if (number.isRealApiRental && number.activationId != null) {
            startRealSmsPolling(number)
        }
    }

    private fun startRealSmsPolling(number: VirtualNumberEntity) {
        realPollingJob?.cancel()
        val activationId = number.activationId ?: return
        val serviceName = SmsActivationApi.AVAILABLE_SERVICES.find { it.code == number.serviceCode }?.name ?: number.serviceCode

        realPollingJob = viewModelScope.launch {
            repository.logAudit("API:POLL", "Started live SMS polling for ID: $activationId (${number.phoneNumber})")
            var attempt = 0
            while (isActive && _isPollingActive.value) {
                attempt++
                _pollingAttempt.value = attempt
                val result = repository.pollRealSmsStatus(
                    activationId = activationId,
                    phoneNumber = number.phoneNumber,
                    serviceName = serviceName,
                    apiKey = _apiKey.value,
                    baseUrl = _baseUrl.value
                )

                if (result.isSuccess) {
                    when (val status = result.getOrThrow()) {
                        is SmsStatusResult.CodeReceived -> {
                            _selectedNumber.value = number.copy(rentalStatus = "CODE_RECEIVED")
                            break // Successfully received code!
                        }
                        is SmsStatusResult.Canceled -> {
                            _selectedNumber.value = number.copy(rentalStatus = "CANCELED")
                            break
                        }
                        else -> {
                            // Still waiting or non-fatal status
                        }
                    }
                }
                delay(4000) // Poll every 4 seconds per SMS activation guidelines
            }
        }
    }

    fun clearSelectedNumber() {
        messagesJob?.cancel()
        realPollingJob?.cancel()
        _selectedNumber.value = null
        _messagesForSelected.value = emptyList()
        _pollingAttempt.value = 0
    }

    fun setCountryFilter(countryCode: String) {
        _selectedCountryFilter.value = countryCode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun forceRefreshDailyCycle() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.ensureDailyQuota(forceRefresh = true)
            updateMeta()
            _isRefreshing.value = false
        }
    }

    fun sendTestOtp(serviceName: String? = null, customCode: String? = null) {
        val current = _selectedNumber.value ?: return
        viewModelScope.launch {
            repository.injectIncomingOtp(
                phoneNumber = current.phoneNumber,
                serviceName = serviceName,
                customCode = customCode
            )
            updateMeta()
        }
    }

    fun togglePolling() {
        val next = !_isPollingActive.value
        _isPollingActive.value = next
        val current = _selectedNumber.value
        if (next) {
            if (current != null && current.isRealApiRental && current.activationId != null) {
                startRealSmsPolling(current)
            } else {
                startSimulatedPolling()
            }
        } else {
            realPollingJob?.cancel()
            simulatedPollingJob?.cancel()
        }
    }

    private fun startSimulatedPolling() {
        simulatedPollingJob?.cancel()
        simulatedPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(12000)
                if (_isPollingActive.value) {
                    val current = _selectedNumber.value
                    if (current != null && !current.isRealApiRental) {
                        if (kotlin.random.Random.nextFloat() < 0.35f) {
                            repository.injectIncomingOtp(current.phoneNumber)
                        }
                    }
                }
            }
        }
    }

    fun clearHistory() {
        val current = _selectedNumber.value ?: return
        viewModelScope.launch {
            repository.clearMessagesForNumber(current.phoneNumber)
        }
    }

    fun toggleAuditDialog(show: Boolean) {
        _showAuditDialog.value = show
    }

    fun toggleSimulateDialog(show: Boolean) {
        _showSimulateDialog.value = show
    }

    fun toggleApiKeyDialog(show: Boolean) {
        _showApiKeyDialog.value = show
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
        realPollingJob?.cancel()
        simulatedPollingJob?.cancel()
    }

    class Factory(private val repository: NumberRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
