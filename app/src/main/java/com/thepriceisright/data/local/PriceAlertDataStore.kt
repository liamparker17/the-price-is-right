package com.thepriceisright.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thepriceisright.domain.model.BarcodeFormat
import com.thepriceisright.domain.model.PriceAlert
import com.thepriceisright.domain.model.PriceSnapshot
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.model.WeightUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.priceAlertDataStore: DataStore<Preferences> by preferencesDataStore(name = "price_alerts")

@Singleton
class PriceAlertDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    private val alertsKey = stringPreferencesKey("alerts")
    private val listType = Types.newParameterizedType(List::class.java, PriceAlertData::class.java)

    fun getAllAlerts(): Flow<List<PriceAlert>> = context.priceAlertDataStore.data.map { prefs ->
        val json = prefs[alertsKey] ?: "[]"
        deserializeAlerts(json)
    }

    suspend fun saveAlerts(alerts: List<PriceAlert>) {
        context.priceAlertDataStore.edit { prefs ->
            prefs[alertsKey] = serializeAlerts(alerts)
        }
    }

    private fun serializeAlerts(alerts: List<PriceAlert>): String {
        try {
            val dataList = alerts.map { it.toData() }
            val adapter = moshi.adapter<List<PriceAlertData>>(listType)
            return adapter.toJson(dataList)
        } catch (e: Exception) {
            return "[]"
        }
    }

    private fun deserializeAlerts(json: String): List<PriceAlert> {
        try {
            val adapter = moshi.adapter<List<PriceAlertData>>(listType)
            return adapter.fromJson(json)?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }
}

data class PriceAlertData(
    val id: String,
    val productId: String,
    val productName: String,
    val productBrand: String,
    val productBarcode: String,
    val productBarcodeFormat: String,
    val productImageUrl: String?,
    val productCategory: String,
    val productWeight: Double,
    val productWeightUnit: String,
    val targetPrice: String?,
    val lastKnownPrice: String,
    val lastChecked: String,
    val isActive: Boolean,
    val priceHistory: List<PriceSnapshotData>
) {
    fun toDomain(): PriceAlert = PriceAlert(
        id = id,
        product = Product(
            id = productId,
            name = productName,
            brand = productBrand,
            barcode = productBarcode,
            barcodeFormat = BarcodeFormat.valueOf(productBarcodeFormat),
            imageUrl = productImageUrl,
            category = productCategory,
            weight = productWeight,
            weightUnit = WeightUnit.valueOf(productWeightUnit)
        ),
        targetPrice = targetPrice?.let { BigDecimal(it) },
        lastKnownPrice = BigDecimal(lastKnownPrice),
        lastChecked = LocalDateTime.parse(lastChecked),
        isActive = isActive,
        priceHistory = priceHistory.map { it.toDomain() }
    )
}

data class PriceSnapshotData(
    val price: String,
    val retailer: String,
    val timestamp: String
) {
    fun toDomain(): PriceSnapshot = PriceSnapshot(
        price = BigDecimal(price),
        retailer = Retailer.valueOf(retailer),
        timestamp = LocalDateTime.parse(timestamp)
    )
}

fun PriceAlert.toData(): PriceAlertData = PriceAlertData(
    id = id,
    productId = product.id,
    productName = product.name,
    productBrand = product.brand,
    productBarcode = product.barcode,
    productBarcodeFormat = product.barcodeFormat.name,
    productImageUrl = product.imageUrl,
    productCategory = product.category,
    productWeight = product.weight,
    productWeightUnit = product.weightUnit.name,
    targetPrice = targetPrice?.toPlainString(),
    lastKnownPrice = lastKnownPrice.toPlainString(),
    lastChecked = lastChecked.toString(),
    isActive = isActive,
    priceHistory = priceHistory.map { PriceSnapshotData(it.price.toPlainString(), it.retailer.name, it.timestamp.toString()) }
)
