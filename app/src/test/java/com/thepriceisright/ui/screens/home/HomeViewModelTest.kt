package com.thepriceisright.ui.screens.home

import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.PriceRepository
import com.thepriceisright.domain.repository.ProductRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var priceRepository: PriceRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        productRepository = mockk()
        priceRepository = mockk()
        viewModel = HomeViewModel(productRepository, priceRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        val state = viewModel.uiState.value
        assertTrue(state.isIdle)
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
    }

    @Test
    fun `onSearchQueryChanged updates query`() {
        viewModel.onSearchQueryChanged("milk")
        assertEquals("milk", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onSearch with blank query does nothing`() {
        viewModel.onSearch("   ")
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun `onSearch success updates results`() = runTest {
        val products = listOf(
            Product(
                id = "1", name = "Full Cream Milk", brand = "Clover",
                barcode = "6001299012345", barcodeFormat = BarcodeFormat.EAN_13,
                imageUrl = null, category = "Dairy", weight = 1.0,
                weightUnit = WeightUnit.L
            )
        )
        coEvery { productRepository.searchProducts("milk") } returns Resource.Success(products)
        coEvery { priceRepository.getPriceQuotes(any()) } returns Resource.Success(emptyList())

        viewModel.onSearch("milk")

        val state = viewModel.uiState.value
        assertFalse(state.isSearching)
        assertTrue(state.searchResults is Resource.Success)
        assertEquals(1, (state.searchResults as Resource.Success).data.size)
        assertTrue(state.recentSearches.contains("milk"))
    }

    @Test
    fun `onSearch error updates error state`() = runTest {
        coEvery { productRepository.searchProducts("xyz") } returns Resource.Error("Not found")

        viewModel.onSearch("xyz")

        val state = viewModel.uiState.value
        assertTrue(state.searchResults is Resource.Error)
        assertEquals("Not found", (state.searchResults as Resource.Error).message)
    }

    @Test
    fun `onBarcodeLookup success updates state`() = runTest {
        val product = Product(
            id = "1", name = "Test", brand = "Brand",
            barcode = "6001299012345", barcodeFormat = BarcodeFormat.EAN_13,
            imageUrl = null, category = "Test", weight = 1.0,
            weightUnit = WeightUnit.KG
        )
        coEvery { productRepository.lookupBarcode("6001299012345") } returns Resource.Success(product)
        coEvery { priceRepository.getPriceQuotes(product) } returns Resource.Success(emptyList())

        viewModel.onBarcodeLookup("6001299012345")

        val state = viewModel.uiState.value
        assertTrue(state.searchResults is Resource.Success)
    }
}
