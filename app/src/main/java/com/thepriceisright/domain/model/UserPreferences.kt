package com.thepriceisright.domain.model

data class UserPreferences(
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val fuelConfig: FuelConfig = FuelConfig(),
    val preferredRetailers: Set<Retailer> = Retailer.entries.toSet(),
    val notificationsEnabled: Boolean = true,
    val priceAlertNotifications: Boolean = true,
    val loyaltyCardReminders: Boolean = true,
    val dataFriendlyMode: Boolean = false
)

enum class DarkMode {
    LIGHT, DARK, SYSTEM
}
