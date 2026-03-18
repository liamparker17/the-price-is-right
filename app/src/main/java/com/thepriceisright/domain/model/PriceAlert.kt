package com.thepriceisright.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class PriceAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val product: Product,
    val targetPrice: BigDecimal? = null,
    val lastKnownPrice: BigDecimal,
    val lastChecked: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val priceHistory: List<PriceSnapshot> = emptyList()
)

data class PriceSnapshot(
    val price: BigDecimal,
    val retailer: Retailer,
    val timestamp: LocalDateTime
)
