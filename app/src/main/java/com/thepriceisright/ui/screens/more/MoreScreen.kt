package com.thepriceisright.ui.screens.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thepriceisright.ui.navigation.Screen
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + Spacing.sm,
                bottom = padding.calculateBottomPadding(),
                start = Spacing.base,
                end = Spacing.base
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item {
                Text(
                    "Features",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Outlined.CreditCard,
                    title = "Loyalty Cards",
                    subtitle = "Store & scan your loyalty cards",
                    onClick = { onNavigate(Screen.LoyaltyCards.route) }
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Outlined.Favorite,
                    title = "Vitality Deals",
                    subtitle = "Discovery Vitality HealthyFood specials",
                    onClick = { onNavigate(Screen.VitalityDeals.route) }
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Price Alerts",
                    subtitle = "Get notified when prices drop",
                    onClick = { onNavigate(Screen.PriceAlerts.route) }
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Outlined.LocalGasStation,
                    title = "Fuel Calculator",
                    subtitle = "Is the drive worth the savings?",
                    onClick = { onNavigate(Screen.FuelCalculator.route) }
                )
            }

            item {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    "App",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    subtitle = "Dark mode, fuel, notifications",
                    onClick = { onNavigate(Screen.Settings.route) }
                )
            }

            item {
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    "Built in South Africa, for South Africa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.sm)
                )
                Text(
                    "grocify.help@gmail.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.base)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(Spacing.base))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Outlined.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
