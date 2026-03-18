package com.thepriceisright.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>
}

@JsonClass(generateAdapter = true)
data class OpenFoodFactsResponse(
    @Json(name = "code") val code: String,
    @Json(name = "status") val status: Int,
    @Json(name = "status_verbose") val statusVerbose: String,
    @Json(name = "product") val product: OpenFoodFactsProduct?
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProduct(
    @Json(name = "product_name") val productName: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "quantity") val quantity: String?,
    @Json(name = "countries") val countries: String?,
    @Json(name = "categories") val categories: String?,
    @Json(name = "code") val code: String?
)
