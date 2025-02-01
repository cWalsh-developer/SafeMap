
package com.example.safemap.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemap.Model.Streetlight
import com.example.safemap.Model.StreetlightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StreetlightViewModel(private val streetlightRepository: StreetlightRepository) :
    ViewModel() {

    private val _streetlights = MutableStateFlow<List<Streetlight>>(emptyList())
    val streetlights: StateFlow<List<Streetlight>> = _streetlights.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    fun loadStreetlights() {
            _isLoading.value = true
            viewModelScope.launch {
                _error.value = null
                try {
                    val fetchedStreetlights = streetlightRepository.getStreetlights()
                    _streetlights.update {fetchedStreetlights}
                } catch (e: Exception) {
                    Log.e("StreetlightViewModel", "Error loading streetlights", e)
                    _error.value = e
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }