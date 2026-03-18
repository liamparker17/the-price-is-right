package com.thepriceisright.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class PricePerUnit(
    val amount: BigDecimal,
    val unit: WeightUnit,
    val formattedPrice: String
) : Comparable<PricePerUnit> {

    override fun compareTo(other: PricePerUnit): Int = amount.compareTo(other.amount)

    companion object {

        private const val SCALE = 2

        fun calculate(totalPrice: BigDecimal, weight: Double, unit: WeightUnit): PricePerUnit {
            if (weight <= 0.0) {
                return PricePerUnit(
                    amount = BigDecimal.ZERO,
                    unit = unit,
                    formattedPrice = "R0.00/${unit.abbreviation}"
                )
            }

            val normalizedUnit = normalizeUnit(unit)
            val normalizedWeight = convertToBaseUnit(weight, unit)

            val pricePerUnit = if (normalizedWeight.compareTo(BigDecimal.ZERO) != 0) {
                totalPrice.divide(normalizedWeight, SCALE, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            val formatted = "R${pricePerUnit.setScale(SCALE, RoundingMode.HALF_UP)}/${normalizedUnit.abbreviation}"

            return PricePerUnit(
                amount = pricePerUnit,
                unit = normalizedUnit,
                formattedPrice = formatted
            )
        }

        private fun normalizeUnit(unit: WeightUnit): WeightUnit = when (unit) {
            WeightUnit.G, WeightUnit.KG -> WeightUnit.KG
            WeightUnit.ML, WeightUnit.L -> WeightUnit.L
            WeightUnit.UNIT -> WeightUnit.UNIT
        }

        private fun convertToBaseUnit(weight: Double, unit: WeightUnit): BigDecimal {
            val weightDecimal = BigDecimal.valueOf(weight)
            return when (unit) {
                WeightUnit.G -> weightDecimal.divide(BigDecimal(1000), SCALE + 4, RoundingMode.HALF_UP)
                WeightUnit.ML -> weightDecimal.divide(BigDecimal(1000), SCALE + 4, RoundingMode.HALF_UP)
                WeightUnit.KG, WeightUnit.L, WeightUnit.UNIT -> weightDecimal
            }
        }
    }
}
