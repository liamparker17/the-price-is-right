package com.thepriceisright.di

import com.thepriceisright.data.remote.source.CheckersSource
import com.thepriceisright.data.remote.source.PickNPaySource
import com.thepriceisright.data.remote.source.RetailerSource
import com.thepriceisright.data.remote.source.ShopriteSource
import com.thepriceisright.data.remote.source.SparSource
import com.thepriceisright.data.remote.source.WoolworthsSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class RetailerModule {

    @Binds
    @IntoSet
    abstract fun bindCheckersSource(
        checkersSource: CheckersSource
    ): RetailerSource

    @Binds
    @IntoSet
    abstract fun bindPickNPaySource(
        pickNPaySource: PickNPaySource
    ): RetailerSource

    @Binds
    @IntoSet
    abstract fun bindWoolworthsSource(
        woolworthsSource: WoolworthsSource
    ): RetailerSource

    @Binds
    @IntoSet
    abstract fun bindShopriteSource(
        shopriteSource: ShopriteSource
    ): RetailerSource

    @Binds
    @IntoSet
    abstract fun bindSparSource(
        sparSource: SparSource
    ): RetailerSource
}
