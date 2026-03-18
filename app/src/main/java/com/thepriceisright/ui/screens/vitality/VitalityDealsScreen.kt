package com.thepriceisright.ui.screens.vitality

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.thepriceisright.domain.model.*
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalityDealsScreen(
    onBack: () -> Unit
) {
    // Mock vitality deals data for UI demonstration
    val categories = VitalityCategory.entries
    var selectedCategory by remember { mutableStateOf<VitalityCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Vitality Deals")
                        Text(
                            "Discovery HealthyFood",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitalityOrange
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + Spacing.sm,
                bottom = padding.calculateBottomPadding()
            )
        ) {
            // Category filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.base),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category.displayName) }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.base))
            }

            // Checkers section
            item {
                Text(
                    "Checkers Vitality Specials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.sm)
                )
            }

            item {
                EmptyState(
                    icon = Icons.Outlined.LocalOffer,
                    title = "Deals loading...",
                    message = "Vitality deals from Checkers will appear here. Connect your Discovery Vitality account to see qualifying products and cashback percentages.",
                    modifier = Modifier.height(200.dp)
                )
            }

            // Woolworths section
            item {
                Text(
                    "Woolworths Vitality Specials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.sm)
                )
            }

            item {
                EmptyState(
                    icon = Icons.Outlined.LocalOffer,
                    title = "Deals loading...",
                    message = "Vitality deals from Woolworths will appear here. Browse HealthyFood qualifying products and stack cashback with existing promotions.",
                    modifier = Modifier.height(200.dp)
                )
            }

            // Info card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = VitalityOrange.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(Spacing.base)
                ) {
                    Column(modifier = Modifier.padding(Spacing.base)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, null, tint = VitalityOrange)
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                "About Vitality HealthyFood",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            "Discovery Vitality members earn up to 25% cashback on HealthyFood items at Checkers and Woolworths. We show you which qualifying products are currently on special so you can stack your savings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
