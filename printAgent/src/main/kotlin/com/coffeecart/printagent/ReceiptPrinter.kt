package com.coffeecart.printagent

import com.coffeecart.shared.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet

private const val RECEIPT_WIDTH_CHARS = 32 // typical 80mm thermal printer at default font size

fun renderReceiptText(cartName: String, order: Order): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(order.createdAt))
    val divider = "-".repeat(RECEIPT_WIDTH_CHARS)
    val builder = StringBuilder()
    builder.appendLine(cartName)
    builder.appendLine("Order ${order.id.take(8)}")
    builder.appendLine(timestamp)
    builder.appendLine(divider)
    var total = 0.0
    for (item in order.items) {
        val lineTotal = item.product.price * item.quantity
        total += lineTotal
        builder.appendLine("${item.quantity}x ${item.product.name}")
        if (item.comment.isNotBlank()) {
            builder.appendLine("   note: ${item.comment}")
        }
        builder.appendLine("   %.2f".format(lineTotal))
    }
    builder.appendLine(divider)
    builder.appendLine("TOTAL: %.2f".format(total))
    builder.appendLine()
    builder.appendLine()
    return builder.toString()
}

private val candidateFlavors = listOf(
    DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8,
    DocFlavor.BYTE_ARRAY.AUTOSENSE,
)

fun printReceipt(printService: PrintService, receiptText: String) {
    val flavor = candidateFlavors.firstOrNull { printService.isDocFlavorSupported(it) }
        ?: error("Printer '${printService.name}' doesn't support any recognized text print format")
    val bytes = receiptText.toByteArray(Charsets.UTF_8)
    val doc = SimpleDoc(bytes, flavor, null)
    val job = printService.createPrintJob()
    job.print(doc, HashPrintRequestAttributeSet())
}
