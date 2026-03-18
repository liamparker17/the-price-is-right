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
 * RetailerSource implementation for Woolworths.
 *
 * Strategy chain (tried in order):
 * 1. Direct retailer API — reverse-engineered Woolworths/commercetools endpoints
 * 2. Web scraping — JSoup-based HTML parsing
 *
 * If all strategies fail, returns "Price data unavailable" — no mock data.
 * Note: Woolworths is NOT covered by iGrosa, so no iGrosa strategy here.
 */
class WoolworthsSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) : RetailerSource {

    override val retailer: Retailer = Retailer.WOOLWORTHS

    private val strategyChain = StrategyChain(
        listOf(
            RetailerApiStrategy(Retailer.WOOLWORTHS, httpClient, moshi),
            WebScrapingStrategy(Retailer.WOOLWORTHS)
        )
    )

    override suspend fun fetchPrice(productName: String, barcode: String): Result<PriceQuote> {
        return strategyChain.execute(productName, barcode)
    }
}
