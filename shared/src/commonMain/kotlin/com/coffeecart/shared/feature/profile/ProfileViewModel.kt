package com.coffeecart.shared.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing operations and alerts on the Profile tab.
 */
class ProfileViewModel(
    private val repository: CoffeeCartRepository,
) : ViewModel() {

    private val _dialogMessage = MutableStateFlow<String?>(null)
    val dialogMessage: StateFlow<String?> = _dialogMessage.asStateFlow()

    fun getCoffeeCarts() {
        viewModelScope.launch {
            try {
                val carts = repository.getCoffeeCarts()
                if (carts.isEmpty()) {
                    _dialogMessage.value = "No coffee carts found."
                } else {
                    val formatted = carts.joinToString(separator = "\n\n") { cart ->
                        "ID: ${cart.id}\nName: ${cart.name}\n📍 ${cart.address}"
                    }
                    _dialogMessage.value = "Existing Coffee Carts:\n\n$formatted"
                }
            } catch (e: Exception) {
                _dialogMessage.value = "Error fetching carts: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun dismissDialog() {
        _dialogMessage.value = null
    }

    fun addCoffeeCart(name: String, address: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                _dialogMessage.value = "Adding coffee cart..."
                val cart = repository.addCoffeeCart(
                    name = name.trim().ifEmpty { "Default Cart" },
                    address = address.trim().ifEmpty { "Default Street" },
                    imageUrl = imageUrl.trim().ifEmpty { "https://picsum.photos/seed/100/200" },
                )
                _dialogMessage.value = "Successfully Added Coffee Cart!\n\nID: ${cart.id}\nName: ${cart.name}\n📍 ${cart.address}"
            } catch (e: Exception) {
                _dialogMessage.value = "Error adding cart: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun removeCoffeeCart(id: String) {
        viewModelScope.launch {
            try {
                val trimmedId = id.trim()
                if (trimmedId.isEmpty()) {
                    _dialogMessage.value = "Cart ID cannot be empty."
                    return@launch
                }
                _dialogMessage.value = "Removing coffee cart with ID: $trimmedId..."
                val success = repository.removeCoffeeCart(trimmedId)
                if (success) {
                    _dialogMessage.value = "Successfully Removed Coffee Cart with ID: $trimmedId!"
                } else {
                    _dialogMessage.value = "Failed to remove coffee cart. ID '$trimmedId' was not found."
                }
            } catch (e: Exception) {
                _dialogMessage.value = "Error removing cart: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun editCoffeeCart(id: String, name: String, address: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                val trimmedId = id.trim()
                if (trimmedId.isEmpty()) {
                    _dialogMessage.value = "Cart ID cannot be empty."
                    return@launch
                }
                _dialogMessage.value = "Updating coffee cart..."
                val success = repository.updateCoffeeCart(
                    id = trimmedId,
                    name = name.trim().ifEmpty { "Updated Cart" },
                    address = address.trim().ifEmpty { "Updated Street" },
                    imageUrl = imageUrl.trim().ifEmpty { "https://picsum.photos/seed/100/200" },
                )
                if (success) {
                    _dialogMessage.value = "Successfully Updated Coffee Cart!\n\nID: $trimmedId\nName: ${name.trim()}\n📍 ${address.trim()}"
                } else {
                    _dialogMessage.value = "Failed to update. Coffee cart with ID '$trimmedId' was not found."
                }
            } catch (e: Exception) {
                _dialogMessage.value = "Error updating cart: ${e.message ?: "Unknown error"}"
            }
        }
    }
}

