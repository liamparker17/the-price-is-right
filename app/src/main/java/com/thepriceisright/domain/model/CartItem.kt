package com.thepriceisright.domain.model

import java.math.BigDecimal

data class CartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val product: Product,
    val quantity: Int = 1,
    val preferredRetailer: Retailer? = null,
    val notes: String? = null
) {
    fun totalPrice(priceQuote: PriceQuote): BigDecimal =
        priceQuote.price.multiply(BigDecimal(quantity))
}
