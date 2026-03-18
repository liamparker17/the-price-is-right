package com.thepriceisright.ui.screens.lists

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thepriceisright.domain.model.ShoppingListItem
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: String,
    onBack: () -> Unit,
    viewModel: ShoppingListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.list?.name ?: "Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::generateShareCode) {
                        Icon(Icons.Outlined.Share, "Share list")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddItemDialog) {
                Icon(Icons.Filled.Add, "Add item")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(padding))

            uiState.list == null -> {
                ErrorState(
                    message = "List not found",
                    onRetry = onBack,
                    modifier = Modifier.padding(padding)
                )
            }

            uiState.list!!.items.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.PlaylistAdd,
                    title = "Empty list",
                    message = "Tap the + button to add your first item",
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                val list = uiState.list!!
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + Spacing.sm,
                        bottom = padding.calculateBottomPadding() + 80.dp,
                        start = Spacing.base,
                        end = Spacing.base
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    // Progress
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = Spacing.sm)
                        ) {
                            Text(
                                "${list.completedCount} of ${list.totalCount} done",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(Spacing.md))
                            LinearProgressIndicator(
                                progress = { list.progress },
                                modifier = Modifier.weight(1f),
                                color = SavingsGreen
                            )
                        }
                    }

                    // Unchecked items first
                    val unchecked = list.items.filter { !it.isChecked }.sortedBy { it.sortOrder }
                    val checked = list.items.filter { it.isChecked }.sortedBy { it.sortOrder }

                    items(unchecked, key = { it.id }) { item ->
                        ShoppingListItemRow(
                            item = item,
                            onToggle = { viewModel.toggleItem(item.id) },
                            onRemove = { viewModel.removeItem(item.id) }
                        )
                    }

                    if (checked.isNotEmpty()) {
                        item {
                            Text(
                                "Completed (${checked.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs)
                            )
                        }
                        items(checked, key = { it.id }) { item ->
                            ShoppingListItemRow(
                                item = item,
                                onToggle = { viewModel.toggleItem(item.id) },
                                onRemove = { viewModel.removeItem(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Add item dialog
        if (uiState.showAddItemDialog) {
            var itemName by remember { mutableStateOf("") }
            var quantity by remember { mutableStateOf("1") }
            AlertDialog(
                onDismissRequest = viewModel::hideAddItemDialog,
                title = { Text("Add Item") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Item name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.addItem(itemName, quantity.toIntOrNull() ?: 1) },
                        enabled = itemName.isNotBlank()
                    ) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::hideAddItemDialog) { Text("Cancel") }
                }
            )
        }

        // Share dialog
        if (uiState.showShareDialog && uiState.shareCode != null) {
            AlertDialog(
                onDismissRequest = viewModel::hideShareDialog,
                title = { Text("Share Code") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Share this code with family to let them import your list:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(Spacing.md))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                uiState.shareCode!!,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(Spacing.base),
                                letterSpacing = androidx.compose.ui.unit.sp(4)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.shareCode!!))
                        viewModel.hideShareDialog()
                    }) { Text("Copy & Close") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::hideShareDialog) { Text("Close") }
                }
            )
        }
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SavingsGreen
                )
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (item.quantity > 1) "${item.name} ×${item.quantity}" else item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.Close,
                    "Remove",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
