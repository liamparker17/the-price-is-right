package com.thepriceisright.domain.model

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val barcode: String,
    val barcodeFormat: BarcodeFormat,
    val imageUrl: String?,
    val category: String,
    val countryOfOrigin: String = "ZA",
    val weight: Double,
    val weightUnit: WeightUnit,
    val isImported: Boolean = false
)

enum class WeightUnit(val abbreviation: String) {
    KG("kg"),
    G("g"),
    L("L"),
    ML("ml"),
    UNIT("unit");

    val isWeight: Boolean
        get() = this == KG || this == G

    val isVolume: Boolean
        get() = this == L || this == ML
}
