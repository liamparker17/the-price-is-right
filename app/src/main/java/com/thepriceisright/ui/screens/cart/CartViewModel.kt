package com.thepriceisright.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.CartRepository
import com.thepriceisright.domain.repository.UserPreferencesRepository
import com.thepriceisright.domain.usecase.CalculateSmartCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cart: SmartCart = SmartCart(),
    val optimization: Resource<SmartCart>? = null,
    val isOptimizing: Boolean = false,
    val fuelConfig: FuelConfig = FuelConfig()
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val calculateSmartCartUseCase: CalculateSmartCartUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cartRepository.getCart().collect { cart ->
                _uiState.update { it.copy(cart = cart) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getPreferences().collect { prefs ->
                _uiState.update { it.copy(fuelConfig = prefs.fuelConfig) }
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch { cartRepository.removeItem(itemId) }
    }

    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch { cartRepository.updateQuantity(itemId, quantity) }
    }

    fun clearCart() {
        viewModelScope.launch { cartRepository.clearCart() }
    }

    fun optimizeCart() {
        val items = _uiState.value.cart.items
        if (items.isEmpty()) return

        _uiState.update { it.copy(isOptimizing = true, optimization = Resource.Loading()) }

        viewModelScope.launch {
            val result = calculateSmartCartUseCase(items, _uiState.value.fuelConfig)
            _uiState.update { it.copy(optimization = result, isOptimizing = false) }
        }
    }
}
