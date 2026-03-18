package com.thepriceisright.data.remote.strategy

import com.thepriceisright.domain.model.PricePerUnit
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.model.WeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Direct retailer API strategy — uses discovered/reverse-engineered endpoints.
 *
 * These endpoints are found by inspecting network traffic on retailer apps/websites.
 * They are NOT official/documented and may change without notice.
 *
 * Priority 2 — preferred over scraping but secondary to stable third-party APIs.
 */
class RetailerApiStrategy(
    private val retailer: Retailer,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : PricingStrategy {

    override val name: String = "DirectAPI(${retailer.displayName})"
    override val priority: Int = 2

    companion object {
        // Discovered API endpoints per retailer
        // Found via browser DevTools / mitmproxy on retailer apps
        //
        // These are undocumented and will change.
        // When they break, update the URLs and response parsing.
        // Consider monitoring these endpoints for changes.

        private val ENDPOINTS = mapOf(
            // Checkers: SAP Commerce backend (discovered via Sixty60 app)
            // The shoprite-miner project (github.com/danielheyman/shoprite-miner) documents these
            Retailer.CHECKERS to RetailerEndpoint(
                searchUrl = "https://www.checkers.co.za/medusa/v2/search?query=%s&count=5",
                productUrl = "https://www.checkers.co.za/medusa/v2/products/%s",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Checkers/3.0 (Android)"
                )
            ),
            // Shoprite: Same SAP backend as Checkers (Shoprite Holdings)
            Retailer.SHOPRITE to RetailerEndpoint(
                searchUrl = "https://www.shoprite.co.za/medusa/v2/search?query=%s&count=5",
                productUrl = "https://www.shoprite.co.za/medusa/v2/products/%s",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Shoprite/3.0 (Android)"
                )
            ),
            // Pick n Pay: SAP Commerce/Hybris backend
            Retailer.PICK_N_PAY to RetailerEndpoint(
                searchUrl = "https://www.pnp.co.za/pnpstorefront/pnp/en/search/autocomplete/%s?format=json",
                productUrl = "https://www.pnp.co.za/pnpstorefront/pnp/en/product/%s?format=json",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14)"
                )
            ),
            // Woolworths: commercetools GraphQL + Algolia
            Retailer.WOOLWORTHS to RetailerEndpoint(
                searchUrl = "https://www.woolworths.co.za/_next/data/search.json?q=%s",
                productUrl = "https://www.woolworths.co.za/server/searchCategory?searchTerm=%s&pageSize=5",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14)"
                )
            ),
            // SPAR: SPAR2U backend
            Retailer.SPAR to RetailerEndpoint(
                searchUrl = "https://www.spar.co.za/api/search?q=%s",
                productUrl = "https://www.spar.co.za/api/product/%s",
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14)"
                )
            )
        )
    }

    override suspend fun isAvailable(): Boolean {
        return ENDPOINTS.containsKey(retailer)
    }

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = ENDPOINTS[retailer]
                    ?: return@withContext Result.failure(RuntimeException("No endpoint for ${retailer.displayName}"))

                val searchUrl = String.format(
                    endpoint.searchUrl,
                    java.net.URLEncoder.encode(productName, "UTF-8")
                )

                val requestBuilder = Request.Builder().url(searchUrl).get()
                endpoint.headers.forEach { (key, value) ->
                    requestBuilder.addHeader(key, value)
                }

                val response = httpClient.newCall(requestBuilder.build()).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        RuntimeException("${retailer.displayName} API returned ${response.code}")
                    )
                }

                val body = response.body?.string()
                    ?: return@withContext Result.failure(RuntimeException("Empty response from ${retailer.displayName}"))

                // Parse the JSON response
                // Each retailer has different response formats — this is generic parsing
                // that looks for common price patterns in the JSON
                val price = extractPriceFromJson(body)
                    ?: return@withContext Result.failure(
                        RuntimeException("Could not extract price from ${retailer.displayName} response")
                    )

                val extractedName = extractNameFromJson(body) ?: productName
                val (weight, unit) = parseWeightFromName(extractedName)
                val pricePerUnit = PricePerUnit.calculate(price, weight, unit)
                val promotion = extractPromotionFromJson(body)

                Result.success(
                    PriceQuote(
                        retailer = retailer.displayName,
                        retailerLogo = retailer.logoRes,
                        price = price,
                        currency = "ZAR",
                        pricePerUnit = pricePerUnit,
                        lastUpdated = LocalDateTime.now(),
                        inStock = true,
                        isOnPromotion = promotion != null,
                        promotionDetails = promotion
                    )
                )
            } catch (e: Exception) {
                Result.failure(RuntimeException("${retailer.displayName} API failed: ${e.message}", e))
            }
        }
    }

    /**
     * Generic price extraction from JSON.
     * Looks for common keys: "price", "sellingPrice", "currentPrice", "amount"
     */
    private fun extractPriceFromJson(json: String): BigDecimal? {
        val pricePatterns = listOf(
            Regex(""""(?:selling[Pp]rice|current[Pp]rice|price|amount|salePrice)"\s*:\s*(\d+\.?\d*)"""),
            Regex(""""(?:Price|PRICE)"\s*:\s*"?R?\s*(\d+\.?\d*)"?"""),
            Regex("""R\s*(\d+\.?\d*)""")
        )
        for (pattern in pricePatterns) {
            val match = pattern.find(json)
            if (match != null) {
                return match.groupValues[1].toBigDecimalOrNull()
            }
        }
        return null
    }

    private fun extractNameFromJson(json: String): String? {
        val namePatterns = listOf(
            Regex(""""(?:name|product[Nn]ame|title|displayName)"\s*:\s*"([^"]+)""""),
        )
        for (pattern in namePatterns) {
            val match = pattern.find(json)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    private fun extractPromotionFromJson(json: String): String? {
        val promoPatterns = listOf(
            Regex(""""(?:promotion|promo[Tt]ext|specialText|badge)"\s*:\s*"([^"]+)""""),
        )
        for (pattern in promoPatterns) {
            val match = pattern.find(json)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    private fun parseWeightFromName(name: String): Pair<Double, WeightUnit> {
        val lower = name.lowercase()
        val kgMatch = Regex("""(\d+\.?\d*)\s*kg""").find(lower)
        val gMatch = Regex("""(\d+\.?\d*)\s*g(?!r)""").find(lower)
        val lMatch = Regex("""(\d+\.?\d*)\s*l(?!a|e|i|o)""").find(lower)
        val mlMatch = Regex("""(\d+\.?\d*)\s*ml""").find(lower)
        return when {
            kgMatch != null -> Pair(kgMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.KG)
            mlMatch != null -> Pair(mlMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.ML)
            gMatch != null -> Pair(gMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.G)
            lMatch != null -> Pair(lMatch.groupValues[1].toDoubleOrNull() ?: 1.0, WeightUnit.L)
            else -> Pair(1.0, WeightUnit.UNIT)
        }
    }
}

data class RetailerEndpoint(
    val searchUrl: String,
    val productUrl: String,
    val headers: Map<String, String> = emptyMap()
)
