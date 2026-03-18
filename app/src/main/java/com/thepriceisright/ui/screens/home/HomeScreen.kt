package com.thepriceisright.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thepriceisright.domain.model.*
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // App header
        TopAppBar(
            title = {
                Text(
                    "Grocify",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Search bar
        GrocifySearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            onSearch = viewModel::onSearch,
            placeholder = "Search groceries across 5 stores...",
            modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.sm)
        )

        // Content area
        when {
            uiState.isIdle -> {
                // Idle state - show recent searches or welcome
                if (uiState.recentSearches.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.Search,
                        title = "Compare grocery prices",
                        message = "Search for any product to see prices across Checkers, Pick n Pay, Woolworths, SPAR, and Shoprite"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.base),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        item {
                            Text(
                                "Recent Searches",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(uiState.recentSearches) { search ->
                            Surface(
                                onClick = { viewModel.onSearch(search) },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.md)
                                ) {
                                    Icon(
                                        Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.md))
                                    Text(search, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            uiState.isSearching -> {
                LoadingContent(itemCount = 4)
            }

            uiState.searchResults is Resource.Error -> {
                ErrorState(
                    message = (uiState.searchResults as Resource.Error).message,
                    onRetry = { viewModel.onSearch(uiState.searchQuery) }
                )
            }

            uiState.searchResults is Resource.Success -> {
                val products = (uiState.searchResults as Resource.Success<List<Product>>).data
                if (products.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "No products found",
                        message = "Try a different search term or scan a barcode",
                        actionLabel = "Clear Search",
                        onAction = { viewModel.onSearchQueryChanged("") }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.base),
                        verticalArrangement = Arrangement.spacedBy(Spacing.base)
                    ) {
                        itemsIndexed(products) { index, product ->
                            ProductSearchResult(
                                product = product,
                                priceQuotes = uiState.priceQuotes[product.barcode],
                                onClick = { onProductClick(product.barcode) },
                                animationDelay = index * 80
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ProductSearchResult(
    product: Product,
    priceQuotes: Resource<List<PriceQuote>>?,
    onClick: () -> Unit,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 4 }
    ) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.base)) {
                // Product info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${product.brand} · ${product.weight}${product.weightUnit.abbreviation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (product.isImported) {
                        Surface(
                            color = AlertAmber.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "Imported",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlertAmber,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Price quotes section
                when (priceQuotes) {
                    is Resource.Loading, null -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            repeat(3) {
                                ShimmerBox(modifier = Modifier.height(20.dp).width(80.dp))
                            }
                        }
                    }
                    is Resource.Success -> {
                        val sorted = priceQuotes.data.sortedBy { it.price }
                        val cheapest = sorted.firstOrNull()
                        val availableRetailers = sorted.mapNotNull {
                            Retailer.fromDisplayName(it.retailer)
                        }.toSet()

                        // Diamond row
                        RetailerDiamondRow(
                            availableRetailers = availableRetailers,
                            cheapestRetailer = cheapest?.let { Retailer.fromDisplayName(it.retailer) }
                        )

                        if (cheapest != null) {
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "From R${cheapest.price}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SavingsGreen
                                )
                                if (sorted.size > 1) {
                                    val mostExpensive = sorted.last()
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text(
                                        text = "to R${mostExpensive.price}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        Text(
                            "Prices unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
