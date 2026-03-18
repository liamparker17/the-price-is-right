package com.thepriceisright.ui.screens.fuel

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
import com.thepriceisright.domain.model.*
import com.thepriceisright.ui.theme.*
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelCalculatorScreen(
    onBack: () -> Unit
) {
    var vehicleSize by remember { mutableStateOf(VehicleSize.MEDIUM) }
    var fuelType by remember { mutableStateOf(FuelType.PETROL_95) }
    var fuelPrice by remember { mutableStateOf("23.50") }
    var distance by remember { mutableStateOf("") }
    var savings by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.base)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.base)
        ) {
            Text(
                "Is the drive worth the savings?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Calculate whether driving to a cheaper store actually saves you money after fuel costs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.sm))

            // Vehicle size selector
            Text("Vehicle", style = MaterialTheme.typography.titleSmall)
            var vehicleExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = vehicleExpanded, onExpandedChange = { vehicleExpanded = it }) {
                OutlinedTextField(
                    value = vehicleSize.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(vehicleExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = vehicleExpanded, onDismissRequest = { vehicleExpanded = false }) {
                    VehicleSize.entries.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size.displayName) },
                            onClick = { vehicleSize = size; vehicleExpanded = false }
                        )
                    }
                }
            }

            // Fuel price
            OutlinedTextField(
                value = fuelPrice,
                onValueChange = { fuelPrice = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Fuel price per litre (ZAR)") },
                leadingIcon = { Text("R", style = MaterialTheme.typography.bodyLarge) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Distance
            OutlinedTextField(
                value = distance,
                onValueChange = { distance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Extra distance to drive (km)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Potential savings
            OutlinedTextField(
                value = savings,
                onValueChange = { savings = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Potential grocery savings (ZAR)") },
                leadingIcon = { Text("R", style = MaterialTheme.typography.bodyLarge) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val config = FuelConfig(
                        vehicleSize = vehicleSize,
                        fuelType = fuelType,
                        fuelPricePerLitre = fuelPrice.toBigDecimalOrNull() ?: BigDecimal("23.50")
                    )
                    val dist = distance.toDoubleOrNull() ?: 0.0
                    val fuelCost = config.calculateFuelCost(dist * 2) // round trip
                    val savingsAmount = savings.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val netSavings = savingsAmount.subtract(fuelCost)

                    result = if (netSavings > BigDecimal.ZERO) {
                        "Worth it! You save R$netSavings after fuel (R$fuelCost fuel cost)"
                    } else {
                        "Not worth the trip. Fuel cost (R$fuelCost) exceeds savings by R${netSavings.negate()}"
                    }
                },
                enabled = distance.isNotBlank() && savings.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Calculate, null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Calculate")
            }

            // Result
            result?.let { msg ->
                val isWorth = msg.startsWith("Worth")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWorth) SavingsGreen.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isWorth) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                            null,
                            tint = if (isWorth) SavingsGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
