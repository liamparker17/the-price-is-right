package com.thepriceisright.domain.model

import java.math.BigDecimal

data class VitalityDeal(
    val id: String,
    val product: Product,
    val retailer: Retailer,
    val originalPrice: BigDecimal,
    val discountedPrice: BigDecimal,
    val vitalityCashbackPercent: Int,
    val isHealthyFood: Boolean = true,
    val category: VitalityCategory
) {
    val savingsAmount: BigDecimal get() = originalPrice.subtract(discountedPrice)
    val savingsPercent: Int get() {
        if (originalPrice == BigDecimal.ZERO) return 0
        return discountedPrice.multiply(BigDecimal(100))
            .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
            .subtract(BigDecimal(100))
            .negate()
            .toInt()
    }
}

enum class VitalityCategory(val displayName: String) {
    FRUIT_VEG("Fruit & Vegetables"),
    LEAN_PROTEIN("Lean Protein"),
    WHOLE_GRAINS("Whole Grains"),
    LOW_FAT_DAIRY("Low-Fat Dairy"),
    HEALTHY_SNACKS("Healthy Snacks"),
    BEVERAGES("Healthy Beverages");
}
