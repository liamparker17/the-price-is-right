package com.thepriceisright.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.PriceRepository
import com.thepriceisright.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val searchQuery: String = "",
    val searchResults: Resource<List<Product>> = Resource.Loading(null),
    val priceQuotes: Map<String, Resource<List<PriceQuote>>> = emptyMap(),
    val isSearching: Boolean = false,
    val recentSearches: List<String> = emptyList()
) {
    val isIdle: Boolean get() = searchQuery.isEmpty() && !isSearching
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val priceRepository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearch(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isSearching = true, searchResults = Resource.Loading()) }

        viewModelScope.launch {
            val result = productRepository.searchProducts(query)
            _uiState.update { state ->
                val updatedRecent = (listOf(query) + state.recentSearches).distinct().take(10)
                state.copy(
                    searchResults = result,
                    isSearching = false,
                    recentSearches = updatedRecent
                )
            }

            // Fetch prices for each found product
            if (result is Resource.Success) {
                result.data.forEach { product ->
                    fetchPricesForProduct(product)
                }
            }
        }
    }

    fun onBarcodeLookup(barcode: String) {
        _uiState.update { it.copy(isSearching = true, searchResults = Resource.Loading()) }

        viewModelScope.launch {
            when (val result = productRepository.lookupBarcode(barcode)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(
                        searchResults = Resource.Success(listOf(result.data)),
                        isSearching = false
                    )}
                    fetchPricesForProduct(result.data)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(
                        searchResults = Resource.Error(result.message),
                        isSearching = false
                    )}
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun fetchPricesForProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(priceQuotes = state.priceQuotes + (product.barcode to Resource.Loading()))
            }
            val prices = priceRepository.getPriceQuotes(product)
            _uiState.update { state ->
                state.copy(priceQuotes = state.priceQuotes + (product.barcode to prices))
            }
        }
    }
}
