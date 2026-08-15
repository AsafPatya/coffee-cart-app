package com.coffeecart.shared.di

import com.coffeecart.shared.data.repository.FakeCoffeeCartRepository
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import org.koin.dsl.module

val coffeeCartModule = module {
    single<CoffeeCartRepository> { FakeCoffeeCartRepository() }
    factory { CoffeeCartListViewModel(get()) }
}
