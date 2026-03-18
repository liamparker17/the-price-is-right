package com.thepriceisright.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiLoggingInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "ApiLoggingInterceptor"
        private const val USER_AGENT = "ThePriceIsRight-Android/1.0 (South Africa Grocery Price Comparison)"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val request = originalRequest.newBuilder()
            .header("User-Agent", USER_AGENT)
            .build()

        val startTime = System.nanoTime()

        Log.d(TAG, "--> ${request.method} ${request.url}")
        Log.d(TAG, "Headers: ${request.headers}")

        return try {
            val response = chain.proceed(request)
            val duration = (System.nanoTime() - startTime) / 1_000_000

            Log.d(TAG, "<-- ${response.code} ${response.message} (${duration}ms)")
            Log.d(TAG, "URL: ${response.request.url}")

            when (response.code) {
                404 -> {
                    Log.w(TAG, "Product not found (404) for URL: ${request.url}")
                }
                429 -> {
                    Log.w(TAG, "Rate limited (429) for URL: ${request.url}. Retry after: ${response.header("Retry-After", "unknown")}")
                }
                in 500..599 -> {
                    Log.e(TAG, "Server error (${response.code}) for URL: ${request.url}")
                }
            }

            response
        } catch (e: IOException) {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            Log.e(TAG, "<-- FAILED (${duration}ms): ${e.message}")
            throw e
        }
    }
}
