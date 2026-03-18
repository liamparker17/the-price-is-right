package com.thepriceisright.ui.screens.loyalty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thepriceisright.domain.model.*
import com.thepriceisright.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoyaltyCardScreen(
    onBack: () -> Unit,
    viewModel: LoyaltyCardsViewModel = hiltViewModel()
) {
    var selectedProgram by remember { mutableStateOf<LoyaltyProgram?>(null) }
    var barcodeValue by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Loyalty Card") },
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
                .padding(Spacing.base)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.base)
        ) {
            Text("Select loyalty program", style = MaterialTheme.typography.titleSmall)

            // Program selector
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedProgram?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loyalty Program") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    LoyaltyProgram.entries.forEach { program ->
                        DropdownMenuItem(
                            text = { Text(program.displayName) },
                            onClick = {
                                selectedProgram = program
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = barcodeValue,
                onValueChange = { barcodeValue = it },
                label = { Text("Card number / barcode") },
                placeholder = { Text("Scan or enter manually") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cardholderName,
                onValueChange = { cardholderName = it },
                label = { Text("Name on card (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.md))

            Button(
                onClick = {
                    val program = selectedProgram ?: return@Button
                    viewModel.addCard(
                        LoyaltyCard(
                            cardName = program.displayName,
                            programName = program,
                            barcodeValue = barcodeValue,
                            barcodeFormat = BarcodeFormat.EAN_13,
                            cardholderName = cardholderName.ifBlank { null },
                            colorHex = program.colorHex
                        )
                    )
                    onBack()
                },
                enabled = selectedProgram != null && barcodeValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Add Card")
            }
        }
    }
}
