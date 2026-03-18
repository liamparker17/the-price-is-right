package com.thepriceisright.di

import com.thepriceisright.data.remote.api.IgrosaApi
import com.thepriceisright.data.remote.api.OpenFoodFactsApi
import com.thepriceisright.data.remote.api.UpcItemDbApi
import com.thepriceisright.data.remote.interceptor.ApiLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val UPC_ITEM_DB_BASE_URL = "https://api.upcitemdb.com/"
    private const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/"
    private const val IGROSA_BASE_URL = "https://igrosa-api.p.rapidapi.com/"

    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiLoggingInterceptor: ApiLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(apiLoggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("UpcItemDb")
    fun provideUpcItemDbRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(UPC_ITEM_DB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("OpenFoodFacts")
    fun provideOpenFoodFactsRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPEN_FOOD_FACTS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideUpcItemDbApi(
        @Named("UpcItemDb") retrofit: Retrofit
    ): UpcItemDbApi {
        return retrofit.create(UpcItemDbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(
        @Named("OpenFoodFacts") retrofit: Retrofit
    ): OpenFoodFactsApi {
        return retrofit.create(OpenFoodFactsApi::class.java)
    }

    @Provides
    @Singleton
    @Named("Igrosa")
    fun provideIgrosaRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IGROSA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideIgrosaApi(
        @Named("Igrosa") retrofit: Retrofit
    ): IgrosaApi {
        return retrofit.create(IgrosaApi::class.java)
    }
}
