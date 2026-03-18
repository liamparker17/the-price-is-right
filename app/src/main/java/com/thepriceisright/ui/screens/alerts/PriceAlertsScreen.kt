package com.thepriceisright.ui.screens.alerts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thepriceisright.domain.model.PriceAlert
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertsScreen(
    onBack: () -> Unit,
    viewModel: PriceAlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Price Alerts") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            uiState.alerts.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.NotificationsNone,
                    title = "No price alerts",
                    message = "When viewing a product, tap the bell icon to track its price. We'll notify you when it drops.",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + Spacing.sm,
                        bottom = padding.calculateBottomPadding(),
                        start = Spacing.base,
                        end = Spacing.base
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.alerts, key = { it.id }) { alert ->
                        PriceAlertCard(
                            alert = alert,
                            onToggle = { viewModel.toggleAlert(alert) },
                            onRemove = { viewModel.removeAlert(alert.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceAlertCard(
    alert: PriceAlert,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.base)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Last seen: R${alert.lastKnownPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (alert.targetPrice != null) {
                    Text(
                        "Alert below: R${alert.targetPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SavingsGreen
                    )
                }
            }
            Switch(checked = alert.isActive, onCheckedChange = { onToggle() })
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Delete, "Remove alert", modifier = Modifier.size(20.dp))
            }
        }
    }
}
