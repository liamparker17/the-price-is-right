package com.thepriceisright.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Compare", Icons.Filled.Search, Icons.Outlined.Search)
    data object Scanner : Screen("scanner", "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    data object Cart : Screen("cart", "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    data object Lists : Screen("lists", "Lists", Icons.Filled.Checklist, Icons.Outlined.Checklist)
    data object More : Screen("more", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)

    // Sub-screens (no bottom nav)
    data object ProductDetail : Screen("product/{barcode}", "Product", Icons.Filled.Info, Icons.Outlined.Info)
    data object LoyaltyCards : Screen("loyalty_cards", "Loyalty Cards", Icons.Filled.CreditCard, Icons.Outlined.CreditCard)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    data object PriceAlerts : Screen("price_alerts", "Price Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object VitalityDeals : Screen("vitality_deals", "Vitality Deals", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    data object FuelCalculator : Screen("fuel_calculator", "Fuel Calculator", Icons.Filled.LocalGasStation, Icons.Outlined.LocalGasStation)
    data object ShoppingListDetail : Screen("list/{listId}", "List", Icons.Filled.Checklist, Icons.Outlined.Checklist)
    data object AddLoyaltyCard : Screen("add_loyalty_card", "Add Card", Icons.Filled.Add, Icons.Outlined.Add)

    companion object {
        val bottomNavItems = listOf(Home, Scanner, Cart, Lists, More)
    }
}
