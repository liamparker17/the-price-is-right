package com.thepriceisright.di

import com.thepriceisright.data.repository.PriceRepositoryImpl
import com.thepriceisright.data.repository.ProductRepositoryImpl
import com.thepriceisright.domain.repository.PriceRepository
import com.thepriceisright.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(
        priceRepositoryImpl: PriceRepositoryImpl
    ): PriceRepository
}
