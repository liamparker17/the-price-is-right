package com.thepriceisright.data.remote.source

import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer

interface RetailerSource {

    val retailer: Retailer

    suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote>
}
