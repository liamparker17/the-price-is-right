package com.thepriceisright.domain.model

enum class Retailer(
    val displayName: String,
    val logoRes: String,
    val websiteUrl: String,
    val colorHex: String,
    val isSouthAfrican: Boolean = true
) {
    CHECKERS(
        displayName = "Checkers",
        logoRes = "ic_checkers",
        websiteUrl = "https://www.checkers.co.za",
        colorHex = "#E31937"
    ),
    PICK_N_PAY(
        displayName = "Pick n Pay",
        logoRes = "ic_pick_n_pay",
        websiteUrl = "https://www.pnp.co.za",
        colorHex = "#0033A0"
    ),
    WOOLWORTHS(
        displayName = "Woolworths",
        logoRes = "ic_woolworths",
        websiteUrl = "https://www.woolworths.co.za",
        colorHex = "#1B1B1B"
    ),
    SPAR(
        displayName = "SPAR",
        logoRes = "ic_spar",
        websiteUrl = "https://www.spar.co.za",
        colorHex = "#00843D"
    ),
    SHOPRITE(
        displayName = "Shoprite",
        logoRes = "ic_shoprite",
        websiteUrl = "https://www.shoprite.co.za",
        colorHex = "#ED1C24"
    );

    companion object {
        fun fromDisplayName(name: String): Retailer? =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
    }
}
