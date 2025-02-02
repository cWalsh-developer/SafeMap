package com.example.safemap.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel(): ViewModel() {
    private val _isStreetlightEnabled = mutableStateOf(false)
    val isStreetlightEnabled: State<Boolean> = _isStreetlightEnabled

    fun toggleStreetlight() {
        _isStreetlightEnabled.value = !_isStreetlightEnabled.value
    }

}