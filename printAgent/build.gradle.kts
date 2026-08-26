import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

/**
 * Desktop-only app: runs on the PC/tablet at a coffee cart, polls the server for unprinted
 * orders belonging to one cart, and sends each as a receipt to a local USB printer via the
 * OS print queue (javax.print) — no raw printer protocol handling required.
 */
dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.okhttp)
}

compose.desktop {
    application {
        mainClass = "com.coffeecart.printagent.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "CoffeeCartPrintAgent"
            packageVersion = "1.0.0"
        }
    }
}

kotlin {
    jvmToolchain(21)
}
