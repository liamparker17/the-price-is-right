package com.thepriceisright.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences = _preferences.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.getPreferences().collect { prefs ->
                _preferences.value = prefs
            }
        }
    }

    fun updateDarkMode(mode: DarkMode) {
        viewModelScope.launch {
            val updated = _preferences.value.copy(darkMode = mode)
            userPreferencesRepository.updatePreferences(updated)
        }
    }

    fun updateFuelConfig(config: FuelConfig) {
        viewModelScope.launch {
            val updated = _preferences.value.copy(fuelConfig = config)
            userPreferencesRepository.updatePreferences(updated)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _preferences.value.copy(notificationsEnabled = enabled)
            userPreferencesRepository.updatePreferences(updated)
        }
    }

    fun toggleDataFriendlyMode(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _preferences.value.copy(dataFriendlyMode = enabled)
            userPreferencesRepository.updatePreferences(updated)
        }
    }
}
