package com.thepriceisright.data.remote.source

import android.content.Context
import com.thepriceisright.data.remote.api.IgrosaApi
import com.thepriceisright.data.remote.strategy.*
import com.thepriceisright.domain.model.PriceQuote
import com.thepriceisright.domain.model.Retailer
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import com.thepriceisright.BuildConfig
import javax.inject.Inject

/**
 * RetailerSource implementation for Checkers (Shoprite Holdings).
 *
 * Strategy chain (tried in order):
 * 1. iGrosa RapidAPI — third-party aggregator
 * 2. Direct retailer API — reverse-engineered Checkers/Sixty60 endpoints
 * 3. Web scraping — JSoup-based HTML parsing
 *
 * If all strategies fail, returns "Price data unavailable" — no mock data.
 */
class CheckersSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val igrosaApi: IgrosaApi,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : RetailerSource {

    override val retailer: Retailer = Retailer.CHECKERS

    private val strategyChain = StrategyChain(
        listOf(
            IgrosaStrategy(igrosaApi, Retailer.CHECKERS, apiKey = BuildConfig.IGROSA_API_KEY),
            RetailerApiStrategy(Retailer.CHECKERS, httpClient, moshi),
            WebScrapingStrategy(Retailer.CHECKERS)
        )
    )

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        return strategyChain.execute(productName, barcode)
    }
}
