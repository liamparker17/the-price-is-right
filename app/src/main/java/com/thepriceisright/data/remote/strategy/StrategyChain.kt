package com.thepriceisright.data.remote.strategy

import android.util.Log
import com.thepriceisright.domain.model.PriceQuote

/**
 * Executes pricing strategies in priority order.
 * If a strategy fails, falls through to the next one.
 * Logs which strategy succeeded for monitoring.
 */
class StrategyChain(
    private val strategies: List<PricingStrategy>
) {
    private val sorted = strategies.sortedBy { it.priority }

    suspend fun execute(productName: String, barcode: String): Result<PriceQuote> {
        val errors = mutableListOf<String>()

        for (strategy in sorted) {
            try {
                if (!strategy.isAvailable()) {
                    errors.add("${strategy.name}: unavailable")
                    continue
                }

                val result = strategy.fetchPrice(productName, barcode)
                if (result.isSuccess) {
                    Log.d("StrategyChain", "Price found via ${strategy.name} for '$productName'")
                    return result
                } else {
                    errors.add("${strategy.name}: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                errors.add("${strategy.name}: ${e.message}")
            }
        }

        Log.w("StrategyChain", "All strategies failed for '$productName': ${errors.joinToString("; ")}")
        return Result.failure(
            RuntimeException("Price data unavailable for this product")
        )
    }
}
