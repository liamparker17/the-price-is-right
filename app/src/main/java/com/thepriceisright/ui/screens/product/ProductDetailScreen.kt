package com.thepriceisright.ui.screens.product

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun ProductDetailScreen(
    barcode: String,
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Price Comparison") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Price alert button
                    val product = (uiState.product as? Resource.Success)?.data
                    val cheapestPrice = (uiState.prices as? Resource.Success)?.data
                        ?.minByOrNull { it.price }?.price
                    if (product != null && cheapestPrice != null) {
                        IconButton(
                            onClick = { viewModel.createPriceAlert(product, cheapestPrice) }
                        ) {
                            Icon(
                                if (uiState.alertCreated) Icons.Filled.Notifications
                                else Icons.Outlined.NotificationsNone,
                                contentDescription = "Set price alert",
                                tint = if (uiState.alertCreated) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val productState = uiState.product) {
            is Resource.Loading -> {
                LoadingContent(
                    itemCount = 5,
                    modifier = Modifier.padding(padding)
                )
            }

            is Resource.Error -> {
                ErrorState(
                    message = productState.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(padding)
                )
            }

            is Resource.Success -> {
                val product = productState.data

                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + Spacing.base,
                        bottom = Spacing.xl,
                        start = Spacing.base,
                        end = Spacing.base
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Product header card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(Spacing.base)) {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    product.brand,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${product.weight}${product.weightUnit.abbreviation}") },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Scale, null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(product.category) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Category, null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                    if (product.isImported) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("Imported") },
                                            leadingIcon = {
                                                Icon(Icons.Outlined.Flight, null, modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                Text(
                                    "Barcode: $barcode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Section header
                    item {
                        Text(
                            "Prices across retailers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Snackbar for added to cart
                    if (uiState.addedToCart) {
                        item {
                            Surface(
                                color = SavingsGreen.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(Spacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = SavingsGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(Spacing.sm))
                                    Text(
                                        "Added to cart",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SavingsGreen
                                    )
                                }
                            }
                        }
                    }

                    // Price quotes
                    when (val pricesState = uiState.prices) {
                        is Resource.Loading -> {
                            items(5) { PriceCardSkeleton() }
                        }

                        is Resource.Error -> {
                            item {
                                ErrorState(
                                    message = pricesState.message,
                                    onRetry = viewModel::retry
                                )
                            }
                        }

                        is Resource.Success -> {
                            val sorted = pricesState.data
                            if (sorted.isEmpty()) {
                                item {
                                    EmptyState(
                                        icon = Icons.Outlined.PriceChange,
                                        title = "No prices found",
                                        message = "We couldn't find this product at any of the 5 retailers"
                                    )
                                }
                            } else {
                                // Savings summary
                                if (sorted.size >= 2) {
                                    item {
                                        val cheapest = sorted.first()
                                        val mostExpensive = sorted.last()
                                        val savings = mostExpensive.price.subtract(cheapest.price)

                                        Surface(
                                            color = SavingsGreen.copy(alpha = 0.1f),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(Spacing.base),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Filled.Savings,
                                                    contentDescription = null,
                                                    tint = SavingsGreen,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(Modifier.width(Spacing.md))
                                                Column {
                                                    Text(
                                                        "Save up to R$savings",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SavingsGreen
                                                    )
                                                    Text(
                                                        "by buying at ${cheapest.retailer} instead of ${mostExpensive.retailer}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                itemsIndexed(sorted) { index, quote ->
                                    PriceComparisonCard(
                                        quote = quote,
                                        isCheapest = index == 0,
                                        onAddToCart = { viewModel.addToCart(product, quote) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
