package com.thepriceisright.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PricePerUnitTest {

    @Nested
    @DisplayName("calculate()")
    inner class Calculate {

        @Test
        fun `calculates price per kg from grams`() {
            val result = PricePerUnit.calculate(
                totalPrice = BigDecimal("29.99"),
                weight = 500.0,
                unit = WeightUnit.G
            )
            // 29.99 / 0.5 = 59.98 per kg
            assertEquals(BigDecimal("59.98"), result.amount)
            assertEquals(WeightUnit.KG, result.unit)
            assertEquals("R59.98/kg", result.formattedPrice)
        }

        @Test
        fun `calculates price per kg from kg`() {
            val result = PricePerUnit.calculate(
                totalPrice = BigDecimal("89.99"),
                weight = 2.0,
                unit = WeightUnit.KG
            )
            assertEquals(BigDecimal("45.00"), result.amount)
            assertEquals(WeightUnit.KG, result.unit)
        }

        @Test
        fun `calculates price per litre from ml`() {
            val result = PricePerUnit.calculate(
                totalPrice = BigDecimal("32.99"),
                weight = 750.0,
                unit = WeightUnit.ML
            )
            // 32.99 / 0.75 = 43.99 per litre
            assertEquals(BigDecimal("43.99"), result.amount)
            assertEquals(WeightUnit.L, result.unit)
        }

        @Test
        fun `handles zero weight gracefully`() {
            val result = PricePerUnit.calculate(
                totalPrice = BigDecimal("29.99"),
                weight = 0.0,
                unit = WeightUnit.KG
            )
            assertEquals(BigDecimal.ZERO, result.amount)
        }

        @Test
        fun `handles negative weight gracefully`() {
            val result = PricePerUnit.calculate(
                totalPrice = BigDecimal("29.99"),
                weight = -1.0,
                unit = WeightUnit.G
            )
            assertEquals(BigDecimal.ZERO, result.amount)
        }

        @Test
        fun `UNIT type returns price per unit`() {
            val result = PricePerUnit.calculate(
                totalPrice = BigDecimal("5.99"),
                weight = 1.0,
                unit = WeightUnit.UNIT
            )
            assertEquals(BigDecimal("5.99"), result.amount)
            assertEquals(WeightUnit.UNIT, result.unit)
        }
    }

    @Nested
    @DisplayName("compareTo()")
    inner class CompareTo {

        @Test
        fun `correctly compares prices`() {
            val cheaper = PricePerUnit.calculate(BigDecimal("29.99"), 1000.0, WeightUnit.G)
            val expensive = PricePerUnit.calculate(BigDecimal("49.99"), 1000.0, WeightUnit.G)
            assertTrue(cheaper < expensive)
        }

        @Test
        fun `equal prices compare as zero`() {
            val a = PricePerUnit.calculate(BigDecimal("29.99"), 500.0, WeightUnit.G)
            val b = PricePerUnit.calculate(BigDecimal("29.99"), 500.0, WeightUnit.G)
            assertEquals(0, a.compareTo(b))
        }
    }
}
