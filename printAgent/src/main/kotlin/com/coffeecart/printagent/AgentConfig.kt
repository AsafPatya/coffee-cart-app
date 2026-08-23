package com.coffeecart.printagent

import java.io.File
import java.util.Properties

data class AgentConfig(val cartId: String, val cartName: String, val printerName: String)

private val configFile = File(System.getProperty("user.home"), ".coffeecart-print-agent/config.properties")

fun loadAgentConfig(): AgentConfig? {
    if (!configFile.exists()) return null
    val props = Properties()
    configFile.inputStream().use { props.load(it) }
    val cartId = props.getProperty("cartId") ?: return null
    val cartName = props.getProperty("cartName") ?: return null
    val printerName = props.getProperty("printerName") ?: return null
    return AgentConfig(cartId, cartName, printerName)
}

fun saveAgentConfig(config: AgentConfig) {
    configFile.parentFile.mkdirs()
    val props = Properties()
    props.setProperty("cartId", config.cartId)
    props.setProperty("cartName", config.cartName)
    props.setProperty("printerName", config.printerName)
    configFile.outputStream().use { props.store(it, "Coffee Cart Print Agent configuration") }
}

fun clearAgentConfig() {
    configFile.delete()
}
