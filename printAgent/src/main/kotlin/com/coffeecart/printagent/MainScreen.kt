package com.coffeecart.printagent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeecart.shared.domain.OrderRepository
import javax.print.PrintServiceLookup
import kotlinx.coroutines.delay

private const val POLL_INTERVAL_MS = 5000L

private data class PrintLogEntry(val message: String, val isError: Boolean)

@Composable
fun MainScreen(
    config: AgentConfig,
    orderRepository: OrderRepository,
    onReset: () -> Unit,
) {
    var log by remember { mutableStateOf(listOf<PrintLogEntry>()) }
    var isPolling by remember { mutableStateOf(true) }

    LaunchedEffect(config) {
        val printService = PrintServiceLookup.lookupPrintServices(null, null)
            .find { it.name == config.printerName }

        if (printService == null) {
            log = log + PrintLogEntry("Printer '${config.printerName}' not found. Reconfigure the agent.", isError = true)
            isPolling = false
            return@LaunchedEffect
        }

        while (isPolling) {
            try {
                val unprinted = orderRepository.getUnprintedOrders(config.cartId)
                for (order in unprinted) {
                    try {
                        val receiptText = renderReceiptText(config.cartName, order)
                        printReceipt(printService, receiptText)
                        orderRepository.markOrderPrinted(config.cartId, order.id)
                        log = log + PrintLogEntry("Printed order ${order.id.take(8)}", isError = false)
                    } catch (e: Exception) {
                        log = log + PrintLogEntry("Failed to print order ${order.id.take(8)}: ${e.message}", isError = true)
                    }
                }
            } catch (e: Exception) {
                log = log + PrintLogEntry("Failed to fetch orders: ${e.message}", isError = true)
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Coffee Cart Print Agent", style = MaterialTheme.typography.headlineSmall)
        Text("Cart: ${config.cartName}")
        Text("Printer: ${config.printerName}")
        Text(if (isPolling) "Status: polling every ${POLL_INTERVAL_MS / 1000}s" else "Status: stopped")

        Button(onClick = {
            isPolling = false
            clearAgentConfig()
            onReset()
        }) {
            Text("Change cart / printer")
        }

        Text("Recent activity", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(log.asReversed()) { entry ->
                Text(
                    entry.message,
                    color = if (entry.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
