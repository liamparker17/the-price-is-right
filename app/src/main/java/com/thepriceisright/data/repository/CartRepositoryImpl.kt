package com.thepriceisright.data.repository

import com.thepriceisright.data.local.CartDataStore
import com.thepriceisright.domain.model.CartItem
import com.thepriceisright.domain.model.SmartCart
import com.thepriceisright.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val dataStore: CartDataStore
) : CartRepository {

    override fun getCart(): Flow<SmartCart> = dataStore.getCartItems().map { items ->
        SmartCart(items = items)
    }

    override suspend fun addItem(item: CartItem) {
        val currentItems = dataStore.getCartItems().first().toMutableList()

        // Check if the same product is already in the cart
        val existingIndex = currentItems.indexOfFirst { it.product.barcode == item.product.barcode }
        if (existingIndex != -1) {
            // Increase quantity instead of adding a duplicate
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            currentItems.add(item)
        }

        dataStore.saveCartItems(currentItems)
    }

    override suspend fun removeItem(itemId: String) {
        val currentItems = dataStore.getCartItems().first().toMutableList()
        currentItems.removeAll { it.id == itemId }
        dataStore.saveCartItems(currentItems)
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int) {
        val currentItems = dataStore.getCartItems().first().toMutableList()
        val index = currentItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            if (quantity <= 0) {
                currentItems.removeAt(index)
            } else {
                currentItems[index] = currentItems[index].copy(quantity = quantity)
            }
        }
        dataStore.saveCartItems(currentItems)
    }

    override suspend fun clearCart() {
        dataStore.saveCartItems(emptyList())
    }
}
