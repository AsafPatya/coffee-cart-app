package com.coffeecart.shared.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.contract.PlaceDetailsDto
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCartFromGoogleViewModel(
    private val repository: CoffeeCartRepositoryInterface,
) : ViewModel() {

    private val _placeIdInput = MutableStateFlow("")
    val placeIdInput: StateFlow<String> = _placeIdInput.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _fetchedDetails = MutableStateFlow<PlaceDetailsDto?>(null)
    val fetchedDetails: StateFlow<PlaceDetailsDto?> = _fetchedDetails.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isFetchedSuccess = MutableStateFlow(false)
    val isFetchedSuccess: StateFlow<Boolean> = _isFetchedSuccess.asStateFlow()

    fun updatePlaceIdInput(value: String) {
        _placeIdInput.value = value
    }

    fun updateName(value: String) {
        _name.value = value
    }

    fun updateAddress(value: String) {
        _address.value = value
    }

    fun updateImageUrl(value: String) {
        _imageUrl.value = value
    }

    fun updatePhone(value: String) {
        _phone.value = value
    }

    fun fetchPlaceDetails() {
        val placeId = _placeIdInput.value
        if (placeId.isBlank()) return

        _isFetching.value = true
        _statusMessage.value = null
        _isFetchedSuccess.value = false

        viewModelScope.launch {
            try {
                val details = repository.fetchPlaceDetails(placeId)
                if (details != null) {
                    _fetchedDetails.value = details
                    details.name?.let { _name.value = it }
                    details.formattedAddress?.let { _address.value = it }
                    details.photoUrls.firstOrNull()?.let { _imageUrl.value = it }
                    details.phoneNumber?.let { _phone.value = it }
                    _isFetchedSuccess.value = true
                    _statusMessage.value = "Successfully fetched Google Place info!"
                } else {
                    _fetchedDetails.value = null
                    _statusMessage.value = "Failed to find details for Place ID."
                }
            } catch (e: Exception) {
                _fetchedDetails.value = null
                _statusMessage.value = "Error fetching details: ${e.message}"
            } finally {
                _isFetching.value = false
            }
        }
    }

    fun saveCoffeeCart(onSuccess: () -> Unit) {
        val currentName = _name.value.trim()
        val currentAddress = _address.value.trim()
        val currentImageUrl = _imageUrl.value.trim()
        val currentPlaceId = _placeIdInput.value.trim().ifBlank { null }
        val currentPhone = _phone.value.trim().ifBlank { null }

        if (currentName.isBlank() || currentAddress.isBlank()) return

        viewModelScope.launch {
            try {
                repository.addCoffeeCart(
                    name = currentName,
                    address = currentAddress,
                    imageUrl = currentImageUrl.ifEmpty { "https://picsum.photos/seed/100/200" },
                    placeId = currentPlaceId,
                    phone = currentPhone,
                )
                onSuccess()
            } catch (e: Exception) {
                _statusMessage.value = "Error saving cart: ${e.message ?: "Unknown error"}"
                _isFetchedSuccess.value = false
            }
        }
    }
}

