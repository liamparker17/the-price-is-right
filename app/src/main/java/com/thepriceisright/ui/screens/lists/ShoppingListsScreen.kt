package com.thepriceisright.ui.screens.lists

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
import com.thepriceisright.domain.model.ShoppingList
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    onListClick: (String) -> Unit,
    viewModel: ShoppingListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Lists", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = viewModel::showImportDialog) {
                        Icon(Icons.Outlined.Download, "Import list")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showCreateDialog,
                icon = { Icon(Icons.Filled.Add, "Create list") },
                text = { Text("New List") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(padding))

            uiState.lists.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Checklist,
                    title = "No shopping lists",
                    message = "Create a list to organize your grocery shopping. Share lists with family via a simple code.",
                    actionLabel = "Create First List",
                    onAction = viewModel::showCreateDialog,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + Spacing.sm,
                        bottom = padding.calculateBottomPadding() + 80.dp,
                        start = Spacing.base,
                        end = Spacing.base
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    itemsIndexed(uiState.lists) { index, list ->
                        ShoppingListCard(
                            list = list,
                            onClick = { onListClick(list.id) },
                            onDelete = { viewModel.deleteList(list.id) }
                        )
                    }
                }
            }
        }

        // Create list dialog
        if (uiState.showCreateDialog) {
            var listName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = viewModel::hideCreateDialog,
                title = { Text("New Shopping List") },
                text = {
                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        label = { Text("List name") },
                        placeholder = { Text("e.g. Weekly groceries") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.createList(listName) },
                        enabled = listName.isNotBlank()
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::hideCreateDialog) { Text("Cancel") }
                }
            )
        }

        // Import dialog
        if (uiState.showImportDialog) {
            AlertDialog(
                onDismissRequest = viewModel::hideImportDialog,
                title = { Text("Import Shared List") },
                text = {
                    OutlinedTextField(
                        value = uiState.importCode,
                        onValueChange = viewModel::onImportCodeChanged,
                        label = { Text("Share code") },
                        placeholder = { Text("Enter 6-character code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = viewModel::importList,
                        enabled = uiState.importCode.length == 6
                    ) { Text("Import") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::hideImportDialog) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.base)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    list.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${list.completedCount}/${list.totalCount} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (list.isShared) {
                        Spacer(Modifier.width(Spacing.sm))
                        Icon(
                            Icons.Outlined.Share,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (list.totalCount > 0) {
                    Spacer(Modifier.height(Spacing.sm))
                    LinearProgressIndicator(
                        progress = { list.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = SavingsGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Outlined.Delete,
                    "Delete list",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete list?") },
            text = { Text("\"${list.name}\" and all its items will be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
