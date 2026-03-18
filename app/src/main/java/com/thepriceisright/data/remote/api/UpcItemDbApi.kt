package com.thepriceisright.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UpcItemDbApi {

    @GET("trial/lookup")
    suspend fun lookupBarcode(
        @Query("upc") upc: String
    ): Response<UpcLookupResponse>
}

@JsonClass(generateAdapter = true)
data class UpcLookupResponse(
    @Json(name = "code") val code: String,
    @Json(name = "total") val total: Int,
    @Json(name = "offset") val offset: Int,
    @Json(name = "items") val items: List<UpcItem>
)

@JsonClass(generateAdapter = true)
data class UpcItem(
    @Json(name = "ean") val ean: String,
    @Json(name = "title") val title: String,
    @Json(name = "brand") val brand: String,
    @Json(name = "category") val category: String,
    @Json(name = "images") val images: List<String>,
    @Json(name = "weight") val weight: String,
    @Json(name = "size") val size: String
)
