package com.thepriceisright.data.repository

import com.thepriceisright.data.remote.source.RetailerSource
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.model.Retailer
import com.thepriceisright.domain.repository.PriceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val retailerSources: Set<@JvmSuppressWildcards RetailerSource>
) : PriceRepository {

    override suspend fun getPriceQuotes(product: Product): Resource<List<PriceQuote>> {
        return try {
            val results = fetchAllPricesConcurrently(product.name, product.barcode)

            val successfulQuotes = results.mapNotNull { result ->
                result.getOrNull()
            }

            val failures = results.filter { it.isFailure }

            if (successfulQuotes.isEmpty()) {
                val errorMessages = failures.map { it.exceptionOrNull()?.message ?: "Unknown error" }
                Resource.Error("Failed to fetch prices from all retailers: ${errorMessages.joinToString("; ")}")
            } else {
                Resource.Success(successfulQuotes.sortedBy { it.price })
            }
        } catch (e: Exception) {
            Resource.Error("Failed to fetch prices: ${e.message}")
        }
    }

    override suspend fun getPriceQuotesForRetailer(
        product: Product,
        retailer: Retailer
    ): Resource<PriceQuote> {
        val source = retailerSources.firstOrNull { it.retailer == retailer }
            ?: return Resource.Error("Retailer ${retailer.displayName} is not available")

        return try {
            val result = source.fetchPrice(product.name, product.barcode)
            result.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error("Failed to fetch price from ${retailer.displayName}: ${it.message}") }
            )
        } catch (e: Exception) {
            Resource.Error("Failed to fetch price from ${retailer.displayName}: ${e.message}")
        }
    }

    private suspend fun fetchAllPricesConcurrently(
        productName: String,
        barcode: String
    ): List<Result<PriceQuote>> = coroutineScope {
        retailerSources.map { source ->
            async {
                try {
                    source.fetchPrice(productName, barcode)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }.awaitAll()
    }
}
