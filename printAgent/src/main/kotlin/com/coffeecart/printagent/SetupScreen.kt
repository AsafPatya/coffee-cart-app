package com.coffeecart.printagent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.model.CoffeeCart
import javax.print.PrintServiceLookup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    coffeeCartRepository: CoffeeCartRepositoryInterface,
    onConfigured: (AgentConfig) -> Unit,
) {
    var carts by remember { mutableStateOf<List<CoffeeCart>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCart by remember { mutableStateOf<CoffeeCart?>(null) }
    var selectedPrinterName by remember { mutableStateOf<String?>(null) }
    val printerNames = remember { PrintServiceLookup.lookupPrintServices(null, null).map { it.name } }

    LaunchedEffect(Unit) {
        try {
            carts = coffeeCartRepository.getCoffeeCarts()
        } catch (e: Exception) {
            error = "Failed to load coffee carts: ${e.message}"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Coffee Cart Print Agent — Setup", style = MaterialTheme.typography.headlineSmall)

        when {
            error != null -> Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            carts == null -> Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator() }
            else -> {
                LabeledDropdown(
                    label = "Coffee cart",
                    options = carts.orEmpty().map { it.name },
                    selected = selectedCart?.name,
                    onSelected = { name -> selectedCart = carts?.find { it.name == name } },
                )

                LabeledDropdown(
                    label = "Printer",
                    options = printerNames,
                    selected = selectedPrinterName,
                    onSelected = { selectedPrinterName = it },
                )

                if (printerNames.isEmpty()) {
                    Text(
                        "No printers found. Make sure the printer is plugged in and installed, then restart this app.",
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Text("Only run one agent per coffee cart at a time.", style = MaterialTheme.typography.bodySmall)

                Button(
                    enabled = selectedCart != null && selectedPrinterName != null,
                    onClick = {
                        val cart = selectedCart ?: return@Button
                        val printerName = selectedPrinterName ?: return@Button
                        val config = AgentConfig(cartId = cart.id, cartName = cart.name, printerName = printerName)
                        saveAgentConfig(config)
                        onConfigured(config)
                    },
                ) {
                    Text("Start")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}
