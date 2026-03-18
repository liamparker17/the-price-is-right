package com.thepriceisright.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.PriceAlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PriceAlertsUiState(
    val alerts: List<PriceAlert> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PriceAlertsViewModel @Inject constructor(
    private val priceAlertRepository: PriceAlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceAlertsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            priceAlertRepository.getActiveAlerts().collect { alerts ->
                _uiState.update { it.copy(alerts = alerts, isLoading = false) }
            }
        }
    }

    fun removeAlert(id: String) {
        viewModelScope.launch { priceAlertRepository.removeAlert(id) }
    }

    fun toggleAlert(alert: PriceAlert) {
        viewModelScope.launch {
            priceAlertRepository.updateAlert(alert.copy(isActive = !alert.isActive))
        }
    }
}
