package com.thepriceisright.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance
            SettingsSection("Appearance") {
                SettingsItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    trailing = {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            AssistChip(
                                onClick = { expanded = true },
                                label = { Text(prefs.darkMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DarkMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            viewModel.updateDarkMode(mode)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // Notifications
            SettingsSection("Notifications") {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Push Notifications",
                    trailing = {
                        Switch(
                            checked = prefs.notificationsEnabled,
                            onCheckedChange = viewModel::toggleNotifications
                        )
                    }
                )
            }

            // Data
            SettingsSection("Data") {
                SettingsItem(
                    icon = Icons.Outlined.DataSaverOn,
                    title = "Data-Friendly Mode",
                    subtitle = "Reduce image loading and cache more aggressively",
                    trailing = {
                        Switch(
                            checked = prefs.dataFriendlyMode,
                            onCheckedChange = viewModel::toggleDataFriendlyMode
                        )
                    }
                )
            }

            // Fuel
            SettingsSection("Fuel Calculator") {
                SettingsItem(
                    icon = Icons.Outlined.DirectionsCar,
                    title = "Vehicle Size",
                    trailing = {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            AssistChip(
                                onClick = { expanded = true },
                                label = { Text(prefs.fuelConfig.vehicleSize.displayName.substringBefore(" (")) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                VehicleSize.entries.forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text(size.displayName) },
                                        onClick = {
                                            viewModel.updateFuelConfig(prefs.fuelConfig.copy(vehicleSize = size))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
                SettingsItem(
                    icon = Icons.Outlined.LocalGasStation,
                    title = "Fuel Type",
                    trailing = {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            AssistChip(
                                onClick = { expanded = true },
                                label = { Text(prefs.fuelConfig.fuelType.displayName) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                FuelType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName) },
                                        onClick = {
                                            viewModel.updateFuelConfig(prefs.fuelConfig.copy(fuelType = type))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // Privacy
            SettingsSection("Privacy") {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.base)
                ) {
                    Column(modifier = Modifier.padding(Spacing.base)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Lock, null, tint = SavingsGreen)
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Privacy First", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            "No accounts required. No login. No tracking your shopping habits. Your data stays on your phone. Period.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = Spacing.sm)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.sm)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.base, vertical = Spacing.md)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(Spacing.base))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing()
    }
}
