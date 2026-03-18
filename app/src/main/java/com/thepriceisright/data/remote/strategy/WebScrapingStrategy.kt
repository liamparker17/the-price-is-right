package com.thepriceisright.data.remote.strategy

import com.thepriceisright.domain.model.PricePerUnit
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.model.WeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * JSoup-based web scraping strategy.
 * Each retailer has its own CSS selectors and URL patterns.
 *
 * This is the fallback when APIs are unavailable.
 * URLs and selectors WILL need updating as sites change.
 */
class WebScrapingStrategy(
    private val retailer: Retailer,
    private val userAgent: String = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
) : PricingStrategy {

    override val name: String = "Scraper(${retailer.displayName})"
    override val priority: Int = 3  // Lower priority — use when APIs fail

    companion object {
        private const val TIMEOUT_MS = 15_000

        // Search URL patterns per retailer
        // These WILL change — that's the nature of scraping
        private val SEARCH_URLS = mapOf(
            Retailer.CHECKERS to "https://www.checkers.co.za/search/all?q=%s",
            Retailer.PICK_N_PAY to "https://www.pnp.co.za/pnpstorefront/pnp/en/search/?text=%s",
            Retailer.WOOLWORTHS to "https://www.woolworths.co.za/cat?Ntt=%s&Dy=1",
            Retailer.SPAR to "https://www.spar.co.za/search?q=%s",
            Retailer.SHOPRITE to "https://www.shoprite.co.za/search/all?q=%s"
        )

        // CSS selectors per retailer for extracting product data
        // These are best-effort and need regular verification
        private val SELECTORS = mapOf(
            Retailer.CHECKERS to ScrapingSelectors(
                productContainer = ".product-frame, .item-product",
                productName = ".product-frame__name, .item-product__name",
                productPrice = ".special-price__price, .before-special-price, .product-frame__price",
                promotionBadge = ".product-frame__promotion, .promo-badge"
            ),
            Retailer.PICK_N_PAY to ScrapingSelectors(
                productContainer = ".product-list-item, .productCarouselItem",
                productName = ".item-name, .product-name",
                productPrice = ".price, .currentPrice",
                promotionBadge = ".promotion, .smartprice-badge"
            ),
            Retailer.WOOLWORTHS to ScrapingSelectors(
                productContainer = ".product-list__item, .product--card",
                productName = ".product-card__name, .range--title",
                productPrice = ".product-card__price, .price",
                promotionBadge = ".product-card__badge, .promotion"
            ),
            Retailer.SPAR to ScrapingSelectors(
                productContainer = ".product-item, .product-card",
                productName = ".product-item__name, .product-name",
                productPrice = ".product-item__price, .price",
                promotionBadge = ".product-badge, .promo"
            ),
            Retailer.SHOPRITE to ScrapingSelectors(
                // Shoprite shares infrastructure with Checkers (Shoprite Holdings)
                productContainer = ".product-frame, .item-product",
                productName = ".product-frame__name, .item-product__name",
                productPrice = ".special-price__price, .before-special-price, .product-frame__price",
                promotionBadge = ".product-frame__promotion, .promo-badge"
            )
        )
    }

    override suspend fun isAvailable(): Boolean {
        return SEARCH_URLS.containsKey(retailer) && SELECTORS.containsKey(retailer)
    }

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        return withContext(Dispatchers.IO) {
            try {
                val searchUrl = SEARCH_URLS[retailer]
                    ?: return@withContext Result.failure(RuntimeException("No search URL for ${retailer.displayName}"))
                val selectors = SELECTORS[retailer]
                    ?: return@withContext Result.failure(RuntimeException("No selectors for ${retailer.displayName}"))

                val url = String.format(searchUrl, java.net.URLEncoder.encode(productName, "UTF-8"))

                val doc: Document = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get()

                // Find first product matching our search
                val productElements = doc.select(selectors.productContainer)
                if (productElements.isEmpty()) {
                    return@withContext Result.failure(
                        RuntimeException("No products found on ${retailer.displayName} for '$productName'")
                    )
                }

                val firstProduct = productElements.first()!!
                val name = firstProduct.select(selectors.productName).text().ifBlank { productName }
                val priceText = firstProduct.select(selectors.productPrice).text()
                val promoText = firstProduct.select(selectors.promotionBadge).text().ifBlank { null }

                val price = parseSAPrice(priceText)
                    ?: return@withContext Result.failure(
                        RuntimeException("Could not parse price '$priceText' from ${retailer.displayName}")
                    )

                val (weight, unit) = parseWeightFromProductName(name)
                val pricePerUnit = PricePerUnit.calculate(price, weight, unit)

                Result.success(
                    PriceQuote(
                        retailer = retailer.displayName,
                        retailerLogo = retailer.logoRes,
                        price = price,
                        currency = "ZAR",
                        pricePerUnit = pricePerUnit,
                        lastUpdated = LocalDateTime.now(),
                        inStock = true,
                        isOnPromotion = promoText != null,
                        promotionDetails = promoText
                    )
                )
            } catch (e: org.jsoup.HttpStatusException) {
                Result.failure(RuntimeException("${retailer.displayName} returned HTTP ${e.statusCode}", e))
            } catch (e: java.net.SocketTimeoutException) {
                Result.failure(RuntimeException("${retailer.displayName} timed out", e))
            } catch (e: Exception) {
                Result.failure(RuntimeException("Scraping ${retailer.displayName} failed: ${e.message}", e))
            }
        }
    }

    /**
     * Parses South African price formats:
     * "R29.99", "R 29.99", "29.99", "R29,99", "R 1 299.99"
     */
    private fun parseSAPrice(text: String): BigDecimal? {
        val cleaned = text
            .replace(Regex("[Rr]"), "")          // Remove R/r prefix
            .replace(Regex("\\s+"), "")           // Remove spaces
            .replace(Regex("(\\d)\\s+(\\d)"), "$1$2")  // Remove space in "1 299"
            .replace(",", ".")                    // Normalize comma decimal
            .trim()

        // Find the first valid number pattern
        val match = Regex("""(\d+\.?\d*)""").find(cleaned)
        return match?.groupValues?.get(1)?.toBigDecimalOrNull()
    }

    /**
     * Tries to extract weight/volume from product name.
     * e.g. "Clover Full Cream Milk 2L" -> (2.0, L)
     */
    private fun parseWeightFromProductName(name: String): Pair<Double, WeightUnit> {
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

data class ScrapingSelectors(
    val productContainer: String,
    val productName: String,
    val productPrice: String,
    val promotionBadge: String
)
