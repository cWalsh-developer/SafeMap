package com.example.safemap.View

sealed class Screen(val route: String) {
    object LoginScreen : Screen("loginscreen")
    object SignUpScreen : Screen("signupscreen")
    object MapScreen : Screen("mapscreen")
    object MainView: Screen("MainView")

}