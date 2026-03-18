package com.thepriceisright.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.usecase.ScanBarcodeUseCase
import com.thepriceisright.domain.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isCameraActive: Boolean = true,
    val lastScannedBarcode: String? = null,
    val product: Resource<Product>? = null,
    val prices: Resource<List<PriceQuote>>? = null,
    val permissionGranted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val priceRepository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
    }

    fun onBarcodeDetected(barcodeResult: BarcodeResult) {
        if (barcodeResult.rawValue == _uiState.value.lastScannedBarcode) return

        _uiState.update { it.copy(
            lastScannedBarcode = barcodeResult.rawValue,
            isCameraActive = false,
            product = Resource.Loading(),
            prices = null
        )}

        viewModelScope.launch {
            val productResult = scanBarcodeUseCase(barcodeResult)
            _uiState.update { it.copy(product = productResult) }

            if (productResult is Resource.Success) {
                _uiState.update { it.copy(prices = Resource.Loading()) }
                val pricesResult = priceRepository.getPriceQuotes(productResult.data)
                _uiState.update { it.copy(prices = pricesResult) }
            }
        }
    }

    fun onRescanClicked() {
        _uiState.update { it.copy(
            isCameraActive = true,
            lastScannedBarcode = null,
            product = null,
            prices = null,
            errorMessage = null
        )}
    }
}
