package com.thepriceisright.data.service

import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.repository.PriceRepository
import com.thepriceisright.domain.repository.ProductRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the full price comparison flow:
 * 1. Validate barcode format
 * 2. Look up product information
 * 3. Fetch prices from all retailers concurrently
 * 4. Sort by price per unit
 * 5. Return sorted comparison results
 */
@Singleton
class PriceComparisonService @Inject constructor(
    private val productRepository: ProductRepository,
    private val priceRepository: PriceRepository
) {

    companion object {
        // TODO: Integrate with a live exchange rate API for imported products
        // South African Reserve Bank publishes rates at:
        // https://www.resbank.co.za/en/home/what-we-do/statistics/key-statistics/selected-historical-rates
        private val USD_TO_ZAR = BigDecimal("18.50")
        private val EUR_TO_ZAR = BigDecimal("20.10")
        private val GBP_TO_ZAR = BigDecimal("23.40")
    }

    /**
     * Performs a full price comparison for the given barcode.
     *
     * @param barcode The product barcode (EAN-13 or UPC-A)
     * @return Resource wrapping a sorted list of PriceQuote results
     */
    suspend fun comparePrice(barcode: String): Resource<List<PriceQuote>> {
        // Step 1: Validate barcode format
        if (!isValidBarcode(barcode)) {
            return Resource.Error("Invalid barcode format. Expected EAN-13 (13 digits) or UPC-A (12 digits).")
        }

        // Step 2: Look up product information
        val productResult = productRepository.lookupBarcode(barcode)
        val product = when (productResult) {
            is Resource.Success -> productResult.data
            is Resource.Error -> return Resource.Error(
                "Could not identify product: ${productResult.message}"
            )
            is Resource.Loading -> return Resource.Loading()
        }

        // Step 3: Fetch prices from all retailers concurrently
        val pricesResult = priceRepository.getPriceQuotes(product)
        val prices = when (pricesResult) {
            is Resource.Success -> pricesResult.data
            is Resource.Error -> return Resource.Error(
                "Could not fetch prices: ${pricesResult.message}"
            )
            is Resource.Loading -> return Resource.Loading()
        }

        // Step 4: Convert any non-ZAR prices and sort by price per unit
        val normalizedPrices = prices.map { quote ->
            if (quote.currency != "ZAR") {
                convertToZar(quote)
            } else {
                quote
            }
        }

        val sortedByUnitPrice = sortByPricePerUnit(normalizedPrices, product)

        // Step 5: Return sorted price quotes
        return Resource.Success(sortedByUnitPrice)
    }

    /**
     * Validates that the barcode is a valid EAN-13 or UPC-A format.
     */
    private fun isValidBarcode(barcode: String): Boolean {
        if (barcode.length !in 12..13) return false
        if (!barcode.all { it.isDigit() }) return false
        return isValidCheckDigit(barcode)
    }

    /**
     * Validates the check digit of an EAN-13 or UPC-A barcode.
     */
    private fun isValidCheckDigit(barcode: String): Boolean {
        val digits = barcode.map { it.digitToInt() }
        val checkDigit = digits.last()
        val sum = digits.dropLast(1).mapIndexed { index, digit ->
            if (index % 2 == 0) digit else digit * 3
        }.sum()
        val calculatedCheck = (10 - (sum % 10)) % 10
        return checkDigit == calculatedCheck
    }

    /**
     * Converts a price quote from a foreign currency to ZAR.
     *
     * TODO: Replace with live exchange rate API integration.
     * Consider using https://api.exchangerate-api.com/v4/latest/ZAR
     * or South African Reserve Bank rates.
     */
    private fun convertToZar(quote: PriceQuote): PriceQuote {
        val conversionRate = when (quote.currency.uppercase()) {
            "USD" -> USD_TO_ZAR
            "EUR" -> EUR_TO_ZAR
            "GBP" -> GBP_TO_ZAR
            "ZAR" -> BigDecimal.ONE
            else -> BigDecimal.ONE // Unknown currency, return as-is
        }

        val convertedPrice = quote.price.multiply(conversionRate).setScale(2, RoundingMode.HALF_UP)
        return quote.copy(
            price = convertedPrice,
            currency = "ZAR"
        )
    }

    /**
     * Sorts price quotes by price per unit when weight/size info is available,
     * otherwise sorts by absolute price.
     */
    private fun sortByPricePerUnit(
        prices: List<PriceQuote>,
        product: Product
    ): List<PriceQuote> {
        // If we can determine weight, sort by price per unit for fair comparison
        return if (product.weight > 0) {
            prices.sortedBy { it.pricePerUnit.amount }
        } else {
            // Fall back to absolute price comparison
            prices.sortedBy { it.price }
        }
    }
}
