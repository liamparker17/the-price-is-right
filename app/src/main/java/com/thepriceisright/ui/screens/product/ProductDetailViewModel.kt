package com.thepriceisright.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.CartRepository
import com.thepriceisright.domain.repository.PriceAlertRepository
import com.thepriceisright.domain.repository.PriceRepository
import com.thepriceisright.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: Resource<Product> = Resource.Loading(),
    val prices: Resource<List<PriceQuote>> = Resource.Loading(),
    val addedToCart: Boolean = false,
    val alertCreated: Boolean = false
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val priceRepository: PriceRepository,
    private val cartRepository: CartRepository,
    private val priceAlertRepository: PriceAlertRepository
) : ViewModel() {

    private val barcode: String = savedStateHandle.get<String>("barcode") ?: ""
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { loadProduct() }

    private fun loadProduct() {
        viewModelScope.launch {
            val productResult = productRepository.lookupBarcode(barcode)
            _uiState.update { it.copy(product = productResult) }

            if (productResult is Resource.Success) {
                val pricesResult = priceRepository.getPriceQuotes(productResult.data)
                _uiState.update { it.copy(
                    prices = when (pricesResult) {
                        is Resource.Success -> Resource.Success(pricesResult.data.sortedBy { q -> q.pricePerUnit })
                        else -> pricesResult
                    }
                )}
            }
        }
    }

    fun addToCart(product: Product, quote: PriceQuote) {
        viewModelScope.launch {
            cartRepository.addItem(CartItem(product = product))
            _uiState.update { it.copy(addedToCart = true) }
        }
    }

    fun createPriceAlert(product: Product, currentPrice: java.math.BigDecimal) {
        viewModelScope.launch {
            priceAlertRepository.addAlert(PriceAlert(
                product = product,
                lastKnownPrice = currentPrice
            ))
            _uiState.update { it.copy(alertCreated = true) }
        }
    }

    fun retry() { loadProduct() }
}
