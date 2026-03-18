package com.thepriceisright.domain.usecase

import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.PriceRepository
import java.math.BigDecimal
import javax.inject.Inject

class CalculateSmartCartUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(
        items: List<CartItem>,
        fuelConfig: FuelConfig
    ): Resource<SmartCart> {
        if (items.isEmpty()) {
            return Resource.Success(SmartCart())
        }

        val allQuotes = mutableMapOf<String, List<PriceQuote>>()

        for (item in items) {
            when (val result = priceRepository.getPriceQuotes(item.product)) {
                is Resource.Success -> allQuotes[item.id] = result.data
                is Resource.Error -> { /* skip items we can't price */ }
                is Resource.Loading -> { /* skip */ }
            }
        }

        if (allQuotes.isEmpty()) {
            return Resource.Error("Could not find prices for any items in your cart")
        }

        // Calculate single-store costs
        val singleStoreOptions = Retailer.entries.mapNotNull { retailer ->
            calculateSingleStoreCost(items, allQuotes, retailer)
        }.sortedBy { it.totalWithFuel }

        val cheapestSingleStore = singleStoreOptions.firstOrNull()

        // Calculate mixed-basket optimization
        val mixedBasket = calculateMixedBasket(items, allQuotes, fuelConfig, cheapestSingleStore)

        return Resource.Success(
            SmartCart(
                items = items,
                singleStoreSuggestion = cheapestSingleStore,
                mixedBasketSuggestion = mixedBasket
            )
        )
    }

    private fun calculateSingleStoreCost(
        items: List<CartItem>,
        allQuotes: Map<String, List<PriceQuote>>,
        retailer: Retailer
    ): StoreSuggestion? {
        val itemPrices = mutableMapOf<String, PriceQuote>()
        var totalCost = BigDecimal.ZERO

        for (item in items) {
            val quotes = allQuotes[item.id] ?: continue
            val quote = quotes.firstOrNull { it.retailer == retailer.displayName && it.inStock }
                ?: return null // This store doesn't have all items

            itemPrices[item.id] = quote
            totalCost = totalCost.add(quote.price.multiply(BigDecimal(item.quantity)))
        }

        return StoreSuggestion(
            retailer = retailer,
            totalCost = totalCost,
            itemPrices = itemPrices
        )
    }

    private fun calculateMixedBasket(
        items: List<CartItem>,
        allQuotes: Map<String, List<PriceQuote>>,
        fuelConfig: FuelConfig,
        cheapestSingleStore: StoreSuggestion?
    ): MixedBasketSuggestion? {
        val assignments = mutableMapOf<Retailer, MutableList<CartItemAssignment>>()
        var totalCost = BigDecimal.ZERO

        for (item in items) {
            val quotes = allQuotes[item.id] ?: continue
            val cheapest = quotes
                .filter { it.inStock }
                .minByOrNull { it.price }
                ?: continue

            val retailer = Retailer.fromDisplayName(cheapest.retailer) ?: continue
            val lineTotal = cheapest.price.multiply(BigDecimal(item.quantity))

            assignments.getOrPut(retailer) { mutableListOf() }
                .add(CartItemAssignment(item, cheapest, lineTotal))

            totalCost = totalCost.add(lineTotal)
        }

        if (assignments.size <= 1) return null // No benefit to splitting

        // Estimate fuel cost: assume 5km between stores on average
        val extraStops = (assignments.size - 1).coerceAtLeast(0)
        val estimatedExtraKm = extraStops * 5.0
        val totalFuelCost = fuelConfig.calculateFuelCost(estimatedExtraKm)

        val savingsVsSingleStore = cheapestSingleStore?.let {
            it.totalCost.subtract(totalCost.add(totalFuelCost))
        } ?: BigDecimal.ZERO

        if (savingsVsSingleStore <= BigDecimal.ZERO) return null // Not worth splitting

        return MixedBasketSuggestion(
            storeAssignments = assignments,
            totalCost = totalCost,
            totalFuelCost = totalFuelCost,
            savingsVsSingleStore = savingsVsSingleStore
        )
    }
}
