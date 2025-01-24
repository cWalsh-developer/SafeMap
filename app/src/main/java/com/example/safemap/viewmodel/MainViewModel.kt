package com.example.safemap.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.safemap.View.DrawerScreenHandler
import com.example.safemap.View.Screen

class MainViewModel: ViewModel(){
    private val _currentScreen = mutableStateOf<Screen>(Screen.MapScreen)

    val currentScreen: MutableState<Screen>
        get() = _currentScreen
    fun setCurrentScreen(screen: Screen)
    {
        _currentScreen.value = screen
    }
}