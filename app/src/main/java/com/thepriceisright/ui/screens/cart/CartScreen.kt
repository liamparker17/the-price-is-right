package com.thepriceisright.ui.screens.cart

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Smart Cart", fontWeight = FontWeight.Bold)
                        if (uiState.cart.totalItems > 0) {
                            Text(
                                "${uiState.cart.totalItems} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.cart.items.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Outlined.DeleteSweep, "Clear cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.cart.items.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = viewModel::optimizeCart,
                        enabled = !uiState.isOptimizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.base)
                            .height(52.dp)
                    ) {
                        if (uiState.isOptimizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Optimizing...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, null)
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Find Cheapest Store")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.cart.isEmpty) {
            EmptyState(
                icon = Icons.Outlined.ShoppingCart,
                title = "Your cart is empty",
                message = "Add items from price comparisons to find the cheapest store for your entire shop",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + Spacing.sm,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                    start = Spacing.base,
                    end = Spacing.base
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Smart cart optimization results
                val optimization = uiState.optimization
                if (optimization is Resource.Success) {
                    val result = optimization.data

                    // Single store suggestion
                    result.singleStoreSuggestion?.let { suggestion ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = SavingsGreen.copy(alpha = 0.08f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(Spacing.base)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Store,
                                            null,
                                            tint = SavingsGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(Spacing.sm))
                                        Text(
                                            "Best Single Store",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(Spacing.sm))
                                    Text(
                                        "${suggestion.retailer.displayName} — R${suggestion.totalCost}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SavingsGreen
                                    )
                                    if (suggestion.estimatedFuelCost > java.math.BigDecimal.ZERO) {
                                        Text(
                                            "+ R${suggestion.estimatedFuelCost} fuel",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Mixed basket suggestion
                    result.mixedBasketSuggestion?.let { mixed ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = PromotionPurple.copy(alpha = 0.08f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(Spacing.base)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            null,
                                            tint = PromotionPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(Spacing.sm))
                                        Text(
                                            "Split Across ${mixed.storeCount} Stores",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(Spacing.sm))
                                    Text(
                                        "R${mixed.totalCost} + R${mixed.totalFuelCost} fuel",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PromotionPurple
                                    )
                                    Text(
                                        "Save R${mixed.savingsVsSingleStore} vs single store",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SavingsGreen
                                    )

                                    Spacer(Modifier.height(Spacing.md))
                                    mixed.storeAssignments.forEach { (retailer, assignments) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = Spacing.xs)
                                        ) {
                                            RetailerDiamond(retailer = retailer)
                                            Spacer(Modifier.width(Spacing.sm))
                                            Text(
                                                "${retailer.displayName}: ${assignments.size} items",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(Spacing.sm)) }
                }

                // Cart items
                item {
                    Text(
                        "Cart Items",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                itemsIndexed(uiState.cart.items) { index, item ->
                    CartItemCard(
                        item = item,
                        onQuantityChange = { qty -> viewModel.updateQuantity(item.id, qty) },
                        onRemove = { viewModel.removeItem(item.id) }
                    )
                }
            }
        }

        // Clear cart confirmation dialog
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Clear cart?") },
                text = { Text("This will remove all ${uiState.cart.totalItems} items from your cart.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearCart()
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveConfirm by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.md)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${item.product.brand} · ${item.product.weight}${item.product.weightUnit.abbreviation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (item.quantity > 1) onQuantityChange(item.quantity - 1)
                        else showRemoveConfirm = true
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (item.quantity == 1) Icons.Outlined.Delete else Icons.Filled.Remove,
                        contentDescription = "Decrease",
                        modifier = Modifier.size(18.dp),
                        tint = if (item.quantity == 1) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.sm)
                )
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Add, "Increase", modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Remove item?") },
            text = { Text("Remove ${item.product.name} from your cart?") },
            confirmButton = {
                TextButton(
                    onClick = { onRemove(); showRemoveConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
