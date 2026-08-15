package com.coffeecart.shared.di

import com.coffeecart.shared.data.repository.KtorCoffeeCartRepository
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

val coffeeCartModule = module {
    single { HttpClient { install(ContentNegotiation) { json() } } }
    single<CoffeeCartRepository> { KtorCoffeeCartRepository(get()) }
    factory { CoffeeCartListViewModel(get()) }
}
