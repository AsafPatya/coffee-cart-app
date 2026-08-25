package com.coffeecart.shared.di

import com.coffeecart.shared.data.repository.ShoppingCartRepository
import com.coffeecart.shared.data.repository.CoffeeCartRepository
import com.coffeecart.shared.data.repository.KtorOrderRepository
import com.coffeecart.shared.data.repository.KtorPaymentRepository
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.domain.OrderRepository
import com.coffeecart.shared.domain.PaymentRepository
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import com.coffeecart.shared.feature.myorder.MyOrderViewModel
import com.coffeecart.shared.feature.orderdashboard.OrderDashboardViewModel
import com.coffeecart.shared.feature.profile.ProfileViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coffeeCartModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    single<CoffeeCartRepositoryInterface> { CoffeeCartRepository(get()) }
    single<ShoppingCartRepositoryInterface> { ShoppingCartRepository() }
    single<OrderRepository> { KtorOrderRepository(get()) }
    single<PaymentRepository> { KtorPaymentRepository(get()) }
    factory { CoffeeCartListViewModel(get()) }
    factory { CoffeeCartDetailsViewModel(get(), get()) }
    factory { ProfileViewModel(get()) }
    factory { OrderDashboardViewModel(get()) }
    factory { MyOrderViewModel(get(), get(), get()) }
}
