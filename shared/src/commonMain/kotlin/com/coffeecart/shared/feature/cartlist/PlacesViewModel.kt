package com.coffeecart.shared.feature.cartlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.BakeryRepositoryInterface
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.location.distanceKm
import com.coffeecart.shared.model.Bakery
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CartCategory { CoffeeCart, Bakery }

sealed interface CoffeeCartListUiState {
    data object Loading : CoffeeCartListUiState
    data class Success(
        val selectedCategory: CartCategory,
        val carts: List<Pair<CoffeeCart, String?>>,
        val bakeries: List<Pair<Bakery, String?>>,
    ) : CoffeeCartListUiState
    data class Error(val message: String) : CoffeeCartListUiState
}

class PlacesViewModel(
    private val cartRepository: CoffeeCartRepositoryInterface,
    private val bakeryRepository: BakeryRepositoryInterface,
) : ViewModel() {
    private val _rawCarts = MutableStateFlow<List<CoffeeCart>?>(null)
    private val _rawBakeries = MutableStateFlow<List<Bakery>?>(null)
    private val _loadError = MutableStateFlow<String?>(null)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _selectedCategory = MutableStateFlow(CartCategory.CoffeeCart)

    val uiState: StateFlow<CoffeeCartListUiState> = combine(
        _rawCarts,
        _rawBakeries,
        _loadError,
        _userLocation,
        _selectedCategory,
    ) { rawCarts, rawBakeries, error, userLocation, selectedCategory ->
        when {
            error != null -> CoffeeCartListUiState.Error(error)
            rawCarts == null || rawBakeries == null -> CoffeeCartListUiState.Loading
            else -> CoffeeCartListUiState.Success(
                selectedCategory = selectedCategory,
                carts = processWithDistance(rawCarts, userLocation, CoffeeCart::latitude, CoffeeCart::longitude),
                bakeries = processWithDistance(rawBakeries, userLocation, Bakery::latitude, Bakery::longitude),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CoffeeCartListUiState.Loading
    )

    init {
        loadCarts()
        loadBakeries()
    }

    private fun <T> processWithDistance(
        items: List<T>,
        userLocation: Pair<Double, Double>?,
        latitude: (T) -> Double?,
        longitude: (T) -> Double?,
    ): List<Pair<T, String?>> {
        val itemsWithDistance = items.map { item ->
            val itemLatitude = latitude(item)
            val itemLongitude = longitude(item)
            val distance = if (userLocation != null && itemLatitude != null && itemLongitude != null) {
                distanceKm(userLocation.first, userLocation.second, itemLatitude, itemLongitude)
            } else {
                null
            }
            val formattedDistance = distance?.let {
                ((it * 10).toInt() / 10.0).toString()
            }
            item to (distance to formattedDistance)
        }

        val sortedItems = if (userLocation != null) {
            itemsWithDistance.sortedBy { (_, pair) -> pair.first ?: Double.MAX_VALUE }
        } else {
            itemsWithDistance
        }

        return sortedItems.map { (item, pair) -> item to pair.second }
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = latitude to longitude
    }

    fun onCategorySelected(category: CartCategory) {
        _selectedCategory.value = category
    }

    fun loadCarts() {
        viewModelScope.launch {
            _loadError.value = null
            try {
                _rawCarts.value = cartRepository.getCoffeeCarts()
            } catch (e: Exception) {
                _loadError.value = e.message ?: "Failed to load coffee carts"
            }
        }
    }

    private fun loadBakeries() {
        viewModelScope.launch {
            try {
                _rawBakeries.value = bakeryRepository.getBakeries()
            } catch (e: Exception) {
                _loadError.value = e.message ?: "Failed to load bakeries"
            }
        }
    }
}
