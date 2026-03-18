package com.thepriceisright.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class PriceQuote(
    val retailer: String,
    val retailerLogo: String?,
    val price: BigDecimal,
    val currency: String = "ZAR",
    val pricePerUnit: PricePerUnit,
    val lastUpdated: LocalDateTime,
    val inStock: Boolean,
    val isOnPromotion: Boolean,
    val promotionDetails: String? = null,
    val storeLocation: String? = null
)
