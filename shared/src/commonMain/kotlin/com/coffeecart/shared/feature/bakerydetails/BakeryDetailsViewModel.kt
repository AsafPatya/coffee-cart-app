package com.coffeecart.shared.feature.bakerydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.BakeryRepositoryInterface
import com.coffeecart.shared.model.Bakery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BakeryDetailsUiState {
    data object Loading : BakeryDetailsUiState
    data class Success(val bakery: Bakery) : BakeryDetailsUiState
    data class Error(val message: String) : BakeryDetailsUiState
}

class BakeryDetailsViewModel(
    private val repository: BakeryRepositoryInterface,
) : ViewModel() {
    private val _uiState = MutableStateFlow<BakeryDetailsUiState>(BakeryDetailsUiState.Loading)
    val uiState: StateFlow<BakeryDetailsUiState> = _uiState.asStateFlow()

    fun loadBakery(id: String) {
        viewModelScope.launch {
            _uiState.value = BakeryDetailsUiState.Loading
            try {
                val bakery = repository.getBakeries().find { it.id == id }
                if (bakery != null) {
                    _uiState.value = BakeryDetailsUiState.Success(bakery)
                } else {
                    _uiState.value = BakeryDetailsUiState.Error("Bakery not found.")
                }
            } catch (e: Exception) {
                _uiState.value = BakeryDetailsUiState.Error(e.message ?: "Failed to load bakery detail.")
            }
        }
    }
}
