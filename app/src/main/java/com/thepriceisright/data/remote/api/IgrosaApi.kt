package com.thepriceisright.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * iGrosa API on RapidAPI — covers Checkers, Shoprite, Pick n Pay.
 * Base URL: https://igrosa-api.p.rapidapi.com/
 *
 * Requires RapidAPI key in X-RapidAPI-Key header.
 */
interface IgrosaApi {

    @GET("search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("store") store: String,  // "checkers", "shoprite", "pnp"
        @Header("X-RapidAPI-Key") apiKey: String = "",
        @Header("X-RapidAPI-Host") host: String = "igrosa-api.p.rapidapi.com"
    ): Response<IgrosaSearchResponse>

    @GET("product")
    suspend fun getProduct(
        @Query("barcode") barcode: String,
        @Query("store") store: String,
        @Header("X-RapidAPI-Key") apiKey: String = "",
        @Header("X-RapidAPI-Host") host: String = "igrosa-api.p.rapidapi.com"
    ): Response<IgrosaProductResponse>
}

@JsonClass(generateAdapter = true)
data class IgrosaSearchResponse(
    @Json(name = "results") val results: List<IgrosaProduct> = emptyList(),
    @Json(name = "total") val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class IgrosaProductResponse(
    @Json(name = "product") val product: IgrosaProduct? = null
)

@JsonClass(generateAdapter = true)
data class IgrosaProduct(
    @Json(name = "name") val name: String = "",
    @Json(name = "brand") val brand: String? = null,
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "image") val image: String? = null,
    @Json(name = "barcode") val barcode: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "in_stock") val inStock: Boolean = true,
    @Json(name = "promotion") val promotion: String? = null,
    @Json(name = "unit_price") val unitPrice: String? = null,
    @Json(name = "store") val store: String = "",
    @Json(name = "weight") val weight: String? = null
)
