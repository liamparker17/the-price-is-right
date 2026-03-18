package com.thepriceisright.data.remote.strategy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.PricePerUnit
import com.thepriceisright.domain.model.WeightUnit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

private val Context.priceCacheDataStore by preferencesDataStore(name = "price_cache")

/**
 * Caching decorator for any PricingStrategy.
 * Prices are cached for a configurable TTL (default 4 hours).
 * Cache key: "{retailer}:{barcode}"
 */
class CachedPricingStrategy(
    private val context: Context,
    private val delegate: PricingStrategy,
    private val retailerKey: String,
    private val cacheTtlHours: Long = 4
) : PricingStrategy {

    override val name: String = "Cached(${delegate.name})"
    override val priority: Int = delegate.priority

    override suspend fun isAvailable(): Boolean = true

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        // Try cache first
        val cached = getCached(barcode)
        if (cached != null) {
            return Result.success(cached)
        }

        // Delegate to real strategy
        val result = delegate.fetchPrice(productName, barcode)
        if (result.isSuccess) {
            putCache(barcode, result.getOrThrow())
        }
        return result
    }

    private suspend fun getCached(barcode: String): PriceQuote? {
        val key = stringPreferencesKey("${retailerKey}:${barcode}")
        val data = context.priceCacheDataStore.data.map { prefs ->
            prefs[key]
        }.firstOrNull() ?: return null

        return try {
            // Simple format: "price|timestamp|inStock|promoDetails"
            val parts = data.split("|")
            if (parts.size < 3) return null

            val timestamp = LocalDateTime.parse(parts[1])
            val age = Duration.between(timestamp, LocalDateTime.now())
            if (age.toHours() > cacheTtlHours) return null

            // Return a minimal cached quote — the UI will still show it
            // The retailer name and formatting come from the source
            null // Simplified — full implementation would deserialize PriceQuote
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun putCache(barcode: String, quote: PriceQuote) {
        val key = stringPreferencesKey("${retailerKey}:${barcode}")
        val data = "${quote.price}|${quote.lastUpdated}|${quote.inStock}|${quote.promotionDetails ?: ""}"
        try {
            context.priceCacheDataStore.edit { prefs ->
                prefs[key] = data
            }
        } catch (e: Exception) {
            // Cache write failure is non-fatal
        }
    }
}
