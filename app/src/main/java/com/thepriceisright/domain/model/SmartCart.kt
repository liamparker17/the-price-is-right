package com.thepriceisright.domain.model

import java.math.BigDecimal

data class SmartCart(
    val items: List<CartItem> = emptyList(),
    val singleStoreSuggestion: StoreSuggestion? = null,
    val mixedBasketSuggestion: MixedBasketSuggestion? = null
) {
    val totalItems: Int get() = items.sumOf { it.quantity }
    val isEmpty: Boolean get() = items.isEmpty()
}

data class StoreSuggestion(
    val retailer: Retailer,
    val totalCost: BigDecimal,
    val itemPrices: Map<String, PriceQuote>,
    val estimatedFuelCost: BigDecimal = BigDecimal.ZERO
) {
    val totalWithFuel: BigDecimal get() = totalCost.add(estimatedFuelCost)
}

data class MixedBasketSuggestion(
    val storeAssignments: Map<Retailer, List<CartItemAssignment>>,
    val totalCost: BigDecimal,
    val totalFuelCost: BigDecimal,
    val savingsVsSingleStore: BigDecimal
) {
    val totalWithFuel: BigDecimal get() = totalCost.add(totalFuelCost)
    val storeCount: Int get() = storeAssignments.size
}

data class CartItemAssignment(
    val cartItem: CartItem,
    val priceQuote: PriceQuote,
    val lineTotal: BigDecimal
)
