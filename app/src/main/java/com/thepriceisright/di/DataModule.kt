package com.thepriceisright.di

import com.thepriceisright.data.repository.CartRepositoryImpl
import com.thepriceisright.data.repository.LoyaltyCardRepositoryImpl
import com.thepriceisright.data.repository.PriceAlertRepositoryImpl
import com.thepriceisright.data.repository.ShoppingListRepositoryImpl
import com.thepriceisright.data.repository.UserPreferencesRepositoryImpl
import com.thepriceisright.domain.repository.CartRepository
import com.thepriceisright.domain.repository.LoyaltyCardRepository
import com.thepriceisright.domain.repository.PriceAlertRepository
import com.thepriceisright.domain.repository.ShoppingListRepository
import com.thepriceisright.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindShoppingListRepository(
        impl: ShoppingListRepositoryImpl
    ): ShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindLoyaltyCardRepository(
        impl: LoyaltyCardRepositoryImpl
    ): LoyaltyCardRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPriceAlertRepository(
        impl: PriceAlertRepositoryImpl
    ): PriceAlertRepository
}
