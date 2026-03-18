package com.thepriceisright.data.remote.source

import android.content.Context
import com.thepriceisright.data.remote.strategy.*
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * RetailerSource implementation for SPAR.
 *
 * Strategy chain (tried in order):
 * 1. Direct retailer API — reverse-engineered SPAR2U endpoints
 * 2. Web scraping — JSoup-based HTML parsing
 *
 * If all strategies fail, returns "Price data unavailable" — no mock data.
 * Note: SPAR is NOT covered by iGrosa, so no iGrosa strategy here.
 */
class SparSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : RetailerSource {

    override val retailer: Retailer = Retailer.SPAR

    private val strategyChain = StrategyChain(
        listOf(
            RetailerApiStrategy(Retailer.SPAR, httpClient, moshi),
            WebScrapingStrategy(Retailer.SPAR)
        )
    )

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        return strategyChain.execute(productName, barcode)
    }
}
