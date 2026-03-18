package com.thepriceisright.domain.repository

import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Product
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.model.Retailer

interface PriceRepository {

    suspend fun getPriceQuotes(product: Product): Resource<List<PriceQuote>>

    suspend fun getPriceQuotesForRetailer(product: Product, retailer: Retailer): Resource<PriceQuote>
}
