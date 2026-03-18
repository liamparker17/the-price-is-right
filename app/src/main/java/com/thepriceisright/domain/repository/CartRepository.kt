package com.thepriceisright.domain.repository

import com.thepriceisright.domain.model.CartItem
import com.thepriceisright.domain.model.SmartCart
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(): Flow<SmartCart>
    suspend fun addItem(item: CartItem)
    suspend fun removeItem(itemId: String)
    suspend fun updateQuantity(itemId: String, quantity: Int)
    suspend fun clearCart()
}
