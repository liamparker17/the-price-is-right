package com.thepriceisright.domain.usecase

import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.repository.PriceRepository

class ComparePricesUseCase(
    private val priceRepository: PriceRepository
) {

    suspend operator fun invoke(product: Product): Resource<List<PriceQuote>> {
        return when (val result = priceRepository.getPriceQuotes(product)) {
            is Resource.Success -> {
                val sorted = result.data.sortedBy { it.pricePerUnit }
                Resource.Success(sorted)
            }
            is Resource.Error -> result
            is Resource.Loading -> result
        }
    }
}
