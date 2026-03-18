package com.thepriceisright.domain.model

data class LoyaltyCard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cardName: String,
    val programName: LoyaltyProgram,
    val barcodeValue: String,
    val barcodeFormat: BarcodeFormat,
    val cardholderName: String? = null,
    val cardNumber: String? = null,
    val colorHex: String
)

enum class LoyaltyProgram(
    val displayName: String,
    val retailer: Retailer?,
    val colorHex: String
) {
    XTRA_SAVINGS("Checkers Xtra Savings", Retailer.CHECKERS, "#E31937"),
    SMART_SHOPPER("Pick n Pay Smart Shopper", Retailer.PICK_N_PAY, "#0033A0"),
    WOOLWORTHS_CARD("Woolworths WRewards", Retailer.WOOLWORTHS, "#1B1B1B"),
    SPAR_REWARDS("SPAR Rewards", Retailer.SPAR, "#00843D"),
    VITALITY("Discovery Vitality", null, "#FF6600"),
    OTHER("Other", null, "#757575");

    companion object {
        fun fromDisplayName(name: String): LoyaltyProgram? =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
    }
}
