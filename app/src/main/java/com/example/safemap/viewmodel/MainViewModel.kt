package com.example.safemap.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.safemap.View.Screen

class MainViewModel: ViewModel(){
    private val _currentScreen: MutableState<Screen> = mutableStateOf(Screen.DrawerScreenHandler.EmergencyContact)

    val currentScreen: MutableState<Screen>
        get() = _currentScreen
    fun setCurrentScreen(screen: Screen)
    {
        _currentScreen.value = screen
    }
}