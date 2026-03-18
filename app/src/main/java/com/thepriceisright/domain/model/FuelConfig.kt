package com.thepriceisright.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class FuelConfig(
    val vehicleSize: VehicleSize = VehicleSize.MEDIUM,
    val fuelType: FuelType = FuelType.PETROL_95,
    val fuelPricePerLitre: BigDecimal = BigDecimal("23.50")
) {
    fun calculateFuelCost(distanceKm: Double): BigDecimal {
        val consumption = vehicleSize.avgConsumptionPer100Km
        val litresNeeded = BigDecimal(distanceKm)
            .multiply(consumption)
            .divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
        return litresNeeded.multiply(fuelPricePerLitre)
            .setScale(2, RoundingMode.HALF_UP)
    }
}

enum class VehicleSize(
    val displayName: String,
    val avgConsumptionPer100Km: BigDecimal
) {
    SMALL("Small (e.g. VW Polo)", BigDecimal("6.5")),
    MEDIUM("Medium (e.g. Toyota Corolla)", BigDecimal("8.0")),
    LARGE("Large (e.g. Ford Ranger)", BigDecimal("11.0")),
    SUV("SUV (e.g. Toyota Fortuner)", BigDecimal("12.5"));
}

enum class FuelType(val displayName: String) {
    PETROL_93("Petrol 93"),
    PETROL_95("Petrol 95"),
    DIESEL_50PPM("Diesel 50ppm"),
    DIESEL_500PPM("Diesel 500ppm");
}
