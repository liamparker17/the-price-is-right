package com.thepriceisright.ui.screens.loyalty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thepriceisright.domain.model.LoyaltyCard
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyCardsScreen(
    onAddCard: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoyaltyCardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loyalty Cards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCard,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Card") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            uiState.cards.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.CreditCard,
                    title = "No loyalty cards",
                    message = "Add your Checkers Xtra Savings, Smart Shopper, WRewards, and SPAR Rewards cards. Scan to add or enter manually.",
                    actionLabel = "Add First Card",
                    onAction = onAddCard,
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
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(uiState.cards, key = { it.id }) { card ->
                        LoyaltyCardItem(
                            card = card,
                            onDelete = { viewModel.deleteCard(card.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoyaltyCardItem(
    card: LoyaltyCard,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val cardColor = try { Color(android.graphics.Color.parseColor(card.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CreditCard,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        card.programName.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (card.cardholderName != null) {
                        Text(
                            card.cardholderName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = Color.White.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            // Barcode display area
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(Radius.sm)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    // Placeholder for barcode rendering
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            card.barcodeValue,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = androidx.compose.ui.unit.sp(2),
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        card.barcodeFormat.name.replace("_", "-"),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove card?") },
            text = { Text("Remove ${card.programName.displayName} from your wallet?") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
