package com.coffeecart.shared.feature.cartlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.location.distanceKm
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CoffeeCartListUiState {
    data object Loading : CoffeeCartListUiState
    data class Success(val carts: List<Pair<CoffeeCart, String?>>) : CoffeeCartListUiState
    data class Error(val message: String) : CoffeeCartListUiState
}

class CoffeeCartListViewModel(
    private val repository: CoffeeCartRepositoryInterface,
) : ViewModel() {
    private val _rawCarts = MutableStateFlow<List<CoffeeCart>?>(null)
    private val _loadError = MutableStateFlow<String?>(null)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    val uiState: StateFlow<CoffeeCartListUiState> = combine(
        _rawCarts,
        _loadError,
        _userLocation
    ) { rawCarts, error, userLocation ->
        when {
            error != null -> CoffeeCartListUiState.Error(error)
            rawCarts == null -> CoffeeCartListUiState.Loading
            else -> CoffeeCartListUiState.Success(processCartsWithDistance(rawCarts, userLocation))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CoffeeCartListUiState.Loading
    )

    init {
        loadCarts()
    }

    private fun processCartsWithDistance(
        rawCarts: List<CoffeeCart>,
        userLocation: Pair<Double, Double>?,
    ): List<Pair<CoffeeCart, String?>> {
        val cartsWithDistance = rawCarts.map { cart ->
            val cartLatitude = cart.latitude
            val cartLongitude = cart.longitude
            val distance = if (userLocation != null && cartLatitude != null && cartLongitude != null) {
                distanceKm(userLocation.first, userLocation.second, cartLatitude, cartLongitude)
            } else {
                null
            }
            val formattedDistance = distance?.let {
                ((it * 10).toInt() / 10.0).toString()
            }
            cart to (distance to formattedDistance)
        }

        val sortedCarts = if (userLocation != null) {
            cartsWithDistance.sortedBy { (_, pair) -> pair.first ?: Double.MAX_VALUE }
        } else {
            cartsWithDistance
        }

        return sortedCarts.map { (cart, pair) -> cart to pair.second }
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = latitude to longitude
    }

    fun loadCarts() {
        viewModelScope.launch {
            _rawCarts.value = null
            _loadError.value = null
            try {
                _rawCarts.value = repository.getCoffeeCarts()
            } catch (e: Exception) {
                _loadError.value = e.message ?: "Failed to load coffee carts"
            }
        }
    }
}
