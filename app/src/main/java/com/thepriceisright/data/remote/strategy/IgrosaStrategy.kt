package com.thepriceisright.data.remote.strategy

import com.thepriceisright.data.remote.api.IgrosaApi
import com.thepriceisright.data.remote.api.IgrosaProduct
import com.thepriceisright.domain.model.PricePerUnit
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.model.WeightUnit
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Pricing strategy using iGrosa RapidAPI.
 * Covers: Checkers, Shoprite, Pick n Pay.
 *
 * This is the fastest path to real data — someone else maintains the scraping.
 */
class IgrosaStrategy(
    private val api: IgrosaApi,
    private val retailer: Retailer,
    private val apiKey: String
) : PricingStrategy {

    override val name: String = "iGrosa(${retailer.displayName})"
    override val priority: Int = 1  // Highest priority — most reliable

    private val storeParam: String = when (retailer) {
        Retailer.CHECKERS -> "checkers"
        Retailer.SHOPRITE -> "shoprite"
        Retailer.PICK_N_PAY -> "pnp"
        else -> throw IllegalArgumentException("iGrosa doesn't support ${retailer.displayName}")
    }

    override suspend fun isAvailable(): Boolean {
        return apiKey.isNotBlank() && retailer in listOf(
            Retailer.CHECKERS, Retailer.SHOPRITE, Retailer.PICK_N_PAY
        )
    }

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        return try {
            // Try barcode lookup first
            val barcodeResponse = api.getProduct(
                barcode = barcode,
                store = storeParam,
                apiKey = apiKey
            )

            val product = if (barcodeResponse.isSuccessful) {
                barcodeResponse.body()?.product
            } else null

            // Fallback to search
            val finalProduct = product ?: run {
                val searchResponse = api.searchProducts(
                    query = productName,
                    store = storeParam,
                    apiKey = apiKey
                )
                if (!searchResponse.isSuccessful) {
                    return Result.failure(RuntimeException("iGrosa search failed: ${searchResponse.code()}"))
                }
                searchResponse.body()?.results?.firstOrNull()
            }

            if (finalProduct == null) {
                return Result.failure(RuntimeException("Product not found on iGrosa for $storeParam"))
            }

            Result.success(mapToQuote(finalProduct))
        } catch (e: Exception) {
            Result.failure(RuntimeException("iGrosa API error: ${e.message}", e))
        }
    }

    private fun mapToQuote(product: IgrosaProduct): PriceQuote {
        val price = BigDecimal.valueOf(product.price)
        val (weight, unit) = parseWeight(product.weight)
        val pricePerUnit = PricePerUnit.calculate(price, weight, unit)

        return PriceQuote(
            retailer = retailer.displayName,
            retailerLogo = retailer.logoRes,
            price = price,
            currency = "ZAR",
            pricePerUnit = pricePerUnit,
            lastUpdated = LocalDateTime.now(),
            inStock = product.inStock,
            isOnPromotion = product.promotion != null,
            promotionDetails = product.promotion,
            storeLocation = null
        )
    }

    private fun parseWeight(weight: String?): Pair<Double, WeightUnit> {
        if (weight.isNullOrBlank()) return Pair(1.0, WeightUnit.UNIT)
        val clean = weight.trim().lowercase()
        val kgMatch = Regex("""([\d.]+)\s*kg""").find(clean)
        val gMatch = Regex("""([\d.]+)\s*g""").find(clean)
        val lMatch = Regex("""([\d.]+)\s*l""").find(clean)
        val mlMatch = Regex("""([\d.]+)\s*ml""").find(clean)
        return when {
            kgMatch != null -> Pair(kgMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.KG)
            gMatch != null -> Pair(gMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.G)
            lMatch != null -> Pair(lMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.L)
            mlMatch != null -> Pair(mlMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.ML)
            else -> Pair(1.0, WeightUnit.UNIT)
        }
    }
}
