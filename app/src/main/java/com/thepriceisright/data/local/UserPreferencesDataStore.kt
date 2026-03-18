package com.thepriceisright.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thepriceisright.domain.model.DarkMode
import com.thepriceisright.domain.model.FuelConfig
import com.thepriceisright.domain.model.FuelType
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.model.UserPreferences
import com.thepriceisright.domain.model.VehicleSize
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val darkModeKey = stringPreferencesKey("dark_mode")
    private val vehicleSizeKey = stringPreferencesKey("vehicle_size")
    private val fuelTypeKey = stringPreferencesKey("fuel_type")
    private val fuelPriceKey = stringPreferencesKey("fuel_price")
    private val preferredRetailersKey = stringPreferencesKey("preferred_retailers")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val priceAlertNotificationsKey = booleanPreferencesKey("price_alert_notifications")
    private val loyaltyCardRemindersKey = booleanPreferencesKey("loyalty_card_reminders")
    private val dataFriendlyModeKey = booleanPreferencesKey("data_friendly_mode")

    fun getPreferences(): Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            darkMode = prefs[darkModeKey]?.let { DarkMode.valueOf(it) } ?: DarkMode.SYSTEM,
            fuelConfig = FuelConfig(
                vehicleSize = prefs[vehicleSizeKey]?.let { VehicleSize.valueOf(it) } ?: VehicleSize.MEDIUM,
                fuelType = prefs[fuelTypeKey]?.let { FuelType.valueOf(it) } ?: FuelType.PETROL_95,
                fuelPricePerLitre = prefs[fuelPriceKey]?.let { BigDecimal(it) } ?: BigDecimal("23.50")
            ),
            preferredRetailers = prefs[preferredRetailersKey]?.let { raw ->
                raw.split(",").filter { it.isNotBlank() }.mapNotNull { name ->
                    try { Retailer.valueOf(name) } catch (_: Exception) { null }
                }.toSet()
            } ?: Retailer.entries.toSet(),
            notificationsEnabled = prefs[notificationsEnabledKey] ?: true,
            priceAlertNotifications = prefs[priceAlertNotificationsKey] ?: true,
            loyaltyCardReminders = prefs[loyaltyCardRemindersKey] ?: true,
            dataFriendlyMode = prefs[dataFriendlyModeKey] ?: false
        )
    }

    suspend fun savePreferences(preferences: UserPreferences) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[darkModeKey] = preferences.darkMode.name
            prefs[vehicleSizeKey] = preferences.fuelConfig.vehicleSize.name
            prefs[fuelTypeKey] = preferences.fuelConfig.fuelType.name
            prefs[fuelPriceKey] = preferences.fuelConfig.fuelPricePerLitre.toPlainString()
            prefs[preferredRetailersKey] = preferences.preferredRetailers.joinToString(",") { it.name }
            prefs[notificationsEnabledKey] = preferences.notificationsEnabled
            prefs[priceAlertNotificationsKey] = preferences.priceAlertNotifications
            prefs[loyaltyCardRemindersKey] = preferences.loyaltyCardReminders
            prefs[dataFriendlyModeKey] = preferences.dataFriendlyMode
        }
    }
}
