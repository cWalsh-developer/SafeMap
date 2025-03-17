
package com.example.safemap.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemap.model.Streetlight
import com.example.safemap.model.StreetlightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StreetlightViewModel(private val streetlightRepository: StreetlightRepository) :
    ViewModel() {

    private val _streetlights = MutableStateFlow<List<Streetlight>>(emptyList())
    val streetlights: StateFlow<List<Streetlight>> = _streetlights.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    val isLoading: State<Boolean> = streetlightRepository.isLoading

    fun loadStreetlights() {
            viewModelScope.launch {
                _error.value = null
                try {
                    val fetchedStreetlights = streetlightRepository.getStreetlights()
                    _streetlights.update {fetchedStreetlights}
                } catch (e: Exception) {
                    Log.e("StreetlightViewModel", "Error loading streetlights", e)
                    _error.value = e
                }
            }
        }
    }