package com.thepriceisright.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class FuelConfigTest {

    @Test
    fun `calculates fuel cost for small car`() {
        val config = FuelConfig(
            vehicleSize = VehicleSize.SMALL,
            fuelType = FuelType.PETROL_95,
            fuelPricePerLitre = BigDecimal("23.50")
        )
        val cost = config.calculateFuelCost(10.0)
        // 10km * 6.5/100 * 23.50 = 15.28
        assertEquals(BigDecimal("15.28"), cost)
    }

    @Test
    fun `calculates fuel cost for SUV`() {
        val config = FuelConfig(
            vehicleSize = VehicleSize.SUV,
            fuelPricePerLitre = BigDecimal("23.50")
        )
        val cost = config.calculateFuelCost(20.0)
        // 20km * 12.5/100 * 23.50 = 58.75
        assertEquals(BigDecimal("58.75"), cost)
    }

    @Test
    fun `zero distance returns zero cost`() {
        val config = FuelConfig()
        val cost = config.calculateFuelCost(0.0)
        assertEquals(BigDecimal("0.00"), cost)
    }
}
