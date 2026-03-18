package com.thepriceisright.ui.screens.scanner

import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.PriceRepository
import com.thepriceisright.domain.usecase.ScanBarcodeUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private lateinit var scanBarcodeUseCase: ScanBarcodeUseCase
    private lateinit var priceRepository: PriceRepository
    private lateinit var viewModel: ScannerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        scanBarcodeUseCase = mockk()
        priceRepository = mockk()
        viewModel = ScannerViewModel(scanBarcodeUseCase, priceRepository)
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state has camera active false and no permission`() {
        val state = viewModel.uiState.value
        assertTrue(state.isCameraActive)
        assertFalse(state.permissionGranted)
    }

    @Test
    fun `onPermissionResult updates state`() {
        viewModel.onPermissionResult(true)
        assertTrue(viewModel.uiState.value.permissionGranted)
    }

    @Test
    fun `duplicate barcode scan is ignored`() = runTest {
        val barcode = BarcodeResult("6001299012345", BarcodeFormat.EAN_13, true)
        val product = Product(
            id = "1", name = "Test", brand = "Brand",
            barcode = "6001299012345", barcodeFormat = BarcodeFormat.EAN_13,
            imageUrl = null, category = "Test", weight = 1.0,
            weightUnit = WeightUnit.KG
        )
        coEvery { scanBarcodeUseCase(barcode) } returns Resource.Success(product)
        coEvery { priceRepository.getPriceQuotes(product) } returns Resource.Success(emptyList())

        viewModel.onBarcodeDetected(barcode)
        viewModel.onBarcodeDetected(barcode) // duplicate

        coVerify(exactly = 1) { scanBarcodeUseCase(barcode) }
    }

    @Test
    fun `onRescanClicked resets state`() {
        viewModel.onRescanClicked()
        val state = viewModel.uiState.value
        assertTrue(state.isCameraActive)
        assertNull(state.lastScannedBarcode)
        assertNull(state.product)
        assertNull(state.prices)
    }
}
