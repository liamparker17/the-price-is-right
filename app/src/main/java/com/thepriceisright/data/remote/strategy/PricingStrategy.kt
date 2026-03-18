package com.thepriceisright.data.remote.strategy

import com.thepriceisright.domain.model.PriceQuote

/**
 * Represents a single approach to fetching a price.
 * Each retailer can have multiple strategies tried in priority order.
 */
interface PricingStrategy {
    val name: String
    val priority: Int  // Lower = tried first
    suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote>
    suspend fun isAvailable(): Boolean  // Health check
}
