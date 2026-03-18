package com.thepriceisright.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thepriceisright.domain.model.BarcodeFormat
import com.thepriceisright.domain.model.CartItem
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.model.WeightUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cartDataStore: DataStore<Preferences> by preferencesDataStore(name = "cart")

@Singleton
class CartDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    private val itemsKey = stringPreferencesKey("cart_items")
    private val listType = Types.newParameterizedType(List::class.java, CartItemData::class.java)

    fun getCartItems(): Flow<List<CartItem>> = context.cartDataStore.data.map { prefs ->
        val json = prefs[itemsKey] ?: "[]"
        deserializeItems(json)
    }

    suspend fun saveCartItems(items: List<CartItem>) {
        context.cartDataStore.edit { prefs ->
            prefs[itemsKey] = serializeItems(items)
        }
    }

    private fun serializeItems(items: List<CartItem>): String {
        try {
            val dataList = items.map { it.toData() }
            val adapter = moshi.adapter<List<CartItemData>>(listType)
            return adapter.toJson(dataList)
        } catch (e: Exception) {
            return "[]"
        }
    }

    private fun deserializeItems(json: String): List<CartItem> {
        try {
            val adapter = moshi.adapter<List<CartItemData>>(listType)
            return adapter.fromJson(json)?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }
}

data class CartItemData(
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
    val quantity: Int,
    val preferredRetailer: String?,
    val notes: String?
) {
    fun toDomain(): CartItem = CartItem(
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
        quantity = quantity,
        preferredRetailer = preferredRetailer?.let { Retailer.valueOf(it) },
        notes = notes
    )
}

fun CartItem.toData(): CartItemData = CartItemData(
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
    quantity = quantity,
    preferredRetailer = preferredRetailer?.name,
    notes = notes
)
