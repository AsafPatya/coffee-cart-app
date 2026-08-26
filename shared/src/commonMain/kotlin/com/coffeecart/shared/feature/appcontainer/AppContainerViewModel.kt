package com.coffeecart.shared.feature.appcontainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppContainerViewModel(
    shoppingCartRepository: ShoppingCartRepositoryInterface,
) : ViewModel() {

    val cartProductCount: StateFlow<Int> = shoppingCartRepository.state
        .map { state -> state.items.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}


