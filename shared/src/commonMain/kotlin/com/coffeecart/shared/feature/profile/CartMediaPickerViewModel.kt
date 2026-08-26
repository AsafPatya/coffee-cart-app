package com.coffeecart.shared.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartMediaPickerViewModel(
    private val repository: CoffeeCartRepositoryInterface,
) : ViewModel() {
    private val _pickedImageBytes = MutableStateFlow<ByteArray?>(null)
    val pickedImageBytes: StateFlow<ByteArray?> = _pickedImageBytes.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        onSuccess: (String) -> Unit,
    ) {
        _pickedImageBytes.value = bytes
        _isUploading.value = true
        viewModelScope.launch {
            try {
                val url = repository.uploadImage(bytes, fileName)
                onSuccess(url)
            } catch (_: Exception) {
                // Return empty string or handle error as fallback
            } finally {
                _isUploading.value = false
            }
        }
    }
}


