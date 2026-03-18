package com.thepriceisright.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors
val CheckersRed = Color(0xFFE31937)
val PnPBlue = Color(0xFF0033A0)
val WoolworthsBlack = Color(0xFF1B1B1B)
val SparGreen = Color(0xFF00843D)
val ShopriteRed = Color(0xFFED1C24)
val VitalityOrange = Color(0xFFFF6600)

// Primary Palette
val GrocifyGreen = Color(0xFF36D399)
val GrocifyGreenDark = Color(0xFF2AB383)
val GrocifyGreenLight = Color(0xFF5EDFB3)

// Light Theme
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F1F3)
val LightOnBackground = Color(0xFF1A1A2E)
val LightOnSurface = Color(0xFF1A1A2E)
val LightOnSurfaceVariant = Color(0xFF6B7280)
val LightOutline = Color(0xFFE2E4E8)
val LightError = Color(0xFFDC2626)

// Dark Theme
val DarkBackground = Color(0xFF0F0F13)
val DarkSurface = Color(0xFF1A1A24)
val DarkSurfaceVariant = Color(0xFF242433)
val DarkOnBackground = Color(0xFFF0EFF5)
val DarkOnSurface = Color(0xFFF0EFF5)
val DarkOnSurfaceVariant = Color(0xFF7A7A8C)
val DarkOutline = Color(0xFF2E2E3E)
val DarkError = Color(0xFFFF4D6D)

// Semantic Colors
val SavingsGreen = Color(0xFF36D399)
val PriceDropBlue = Color(0xFF3B82F6)
val AlertAmber = Color(0xFFF59E0B)
val PromotionPurple = Color(0xFF8B5CF6)
val CheapestGold = Color(0xFFFFD700)

// Retailer color helper
fun retailerColor(retailerName: String): Color = when {
    retailerName.contains("Checkers", true) -> CheckersRed
    retailerName.contains("Pick n Pay", true) || retailerName.contains("PnP", true) -> PnPBlue
    retailerName.contains("Woolworths", true) -> WoolworthsBlack
    retailerName.contains("SPAR", true) -> SparGreen
    retailerName.contains("Shoprite", true) -> ShopriteRed
    else -> GrocifyGreen
}
