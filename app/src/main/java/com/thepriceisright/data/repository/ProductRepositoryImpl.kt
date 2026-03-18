package com.thepriceisright.data.repository

import com.thepriceisright.data.remote.api.OpenFoodFactsApi
import com.thepriceisright.data.remote.api.UpcItemDbApi
import com.thepriceisright.domain.model.BarcodeFormat
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.model.WeightUnit
import com.thepriceisright.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val upcItemDbApi: UpcItemDbApi
) : ProductRepository {

    override suspend fun lookupBarcode(barcode: String): Resource<Product> {
        return try {
            // Try OpenFoodFacts first - better coverage for SA/international products
            val offResult = lookupFromOpenFoodFacts(barcode)
            if (offResult is Resource.Success) {
                return offResult
            }

            // Fallback to UPCitemdb
            val upcResult = lookupFromUpcItemDb(barcode)
            if (upcResult is Resource.Success) {
                return upcResult
            }

            Resource.Error("Product not found in any database for barcode: $barcode")
        } catch (e: Exception) {
            Resource.Error("Failed to lookup product: ${e.message}")
        }
    }

    override suspend fun searchProducts(query: String): Resource<List<Product>> {
        // TODO: Implement product search across APIs
        // OpenFoodFacts supports search via: /cgi/search.pl?search_terms={query}&json=1
        return Resource.Error("Product search not yet implemented")
    }

    private suspend fun lookupFromOpenFoodFacts(barcode: String): Resource<Product> {
        return try {
            val response = openFoodFactsApi.getProduct(barcode)

            if (!response.isSuccessful) {
                return Resource.Error("OpenFoodFacts API error: ${response.code()}")
            }

            val body = response.body() ?: return Resource.Error("Empty response from OpenFoodFacts")

            if (body.status != 1 || body.product == null) {
                return Resource.Error("Product not found on OpenFoodFacts")
            }

            val offProduct = body.product
            val (weight, weightUnit) = parseQuantity(offProduct.quantity)
            val barcodeFormat = determineBarcodeFormat(barcode)

            val product = Product(
                id = barcode,
                name = offProduct.productName ?: "Unknown Product",
                brand = offProduct.brands ?: "Unknown Brand",
                barcode = barcode,
                barcodeFormat = barcodeFormat,
                imageUrl = offProduct.imageUrl,
                category = offProduct.categories ?: "Uncategorized",
                countryOfOrigin = extractCountryCode(offProduct.countries),
                weight = weight,
                weightUnit = weightUnit,
                isImported = !isSouthAfrican(offProduct.countries)
            )

            Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error("OpenFoodFacts lookup failed: ${e.message}")
        }
    }

    private suspend fun lookupFromUpcItemDb(barcode: String): Resource<Product> {
        return try {
            val response = upcItemDbApi.lookupBarcode(barcode)

            if (!response.isSuccessful) {
                return Resource.Error("UPCitemdb API error: ${response.code()}")
            }

            val body = response.body() ?: return Resource.Error("Empty response from UPCitemdb")

            if (body.items.isEmpty()) {
                return Resource.Error("Product not found on UPCitemdb")
            }

            val item = body.items.first()
            val (weight, weightUnit) = parseWeight(item.weight, item.size)
            val barcodeFormat = determineBarcodeFormat(barcode)

            val product = Product(
                id = item.ean,
                name = item.title,
                brand = item.brand,
                barcode = item.ean,
                barcodeFormat = barcodeFormat,
                imageUrl = item.images.firstOrNull(),
                category = item.category,
                weight = weight,
                weightUnit = weightUnit
            )

            Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error("UPCitemdb lookup failed: ${e.message}")
        }
    }

    private fun determineBarcodeFormat(barcode: String): BarcodeFormat {
        return when (barcode.length) {
            13 -> BarcodeFormat.EAN_13
            12 -> BarcodeFormat.UPC_A
            8 -> BarcodeFormat.EAN_8
            else -> BarcodeFormat.UNKNOWN
        }
    }

    /**
     * Parses a quantity string like "500g", "1kg", "2L", "750ml" into weight and unit.
     */
    private fun parseQuantity(quantity: String?): Pair<Double, WeightUnit> {
        if (quantity.isNullOrBlank()) return Pair(1.0, WeightUnit.UNIT)

        val clean = quantity.trim().lowercase()

        val kgRegex = Regex("""([\d.]+)\s*kg""")
        val gRegex = Regex("""([\d.]+)\s*g""")
        val lRegex = Regex("""([\d.]+)\s*l""")
        val mlRegex = Regex("""([\d.]+)\s*ml""")

        return when {
            kgRegex.containsMatchIn(clean) -> {
                val value = kgRegex.find(clean)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                Pair(value, WeightUnit.KG)
            }
            gRegex.containsMatchIn(clean) -> {
                val value = gRegex.find(clean)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                Pair(value, WeightUnit.G)
            }
            lRegex.containsMatchIn(clean) -> {
                val value = lRegex.find(clean)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                Pair(value, WeightUnit.L)
            }
            mlRegex.containsMatchIn(clean) -> {
                val value = mlRegex.find(clean)?.groupValues?.get(1)?.toDoubleOrNull() ?: 1.0
                Pair(value, WeightUnit.ML)
            }
            else -> Pair(1.0, WeightUnit.UNIT)
        }
    }

    /**
     * Parses weight and size strings from UPCitemdb into weight and unit.
     */
    private fun parseWeight(weight: String, size: String): Pair<Double, WeightUnit> {
        val combined = "$weight $size"
        return parseQuantity(combined)
    }

    private fun extractCountryCode(countries: String?): String {
        if (countries.isNullOrBlank()) return "ZA"
        val lower = countries.lowercase()
        return when {
            lower.contains("south africa") || lower.contains("za") -> "ZA"
            lower.contains("united states") || lower.contains("us") -> "US"
            lower.contains("united kingdom") || lower.contains("uk") -> "GB"
            else -> "ZA"
        }
    }

    private fun isSouthAfrican(countries: String?): Boolean {
        if (countries.isNullOrBlank()) return true // Assume local if unknown
        val lower = countries.lowercase()
        return lower.contains("south africa") || lower.contains("za")
    }
}
