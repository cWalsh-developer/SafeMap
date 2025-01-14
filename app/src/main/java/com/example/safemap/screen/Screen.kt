package com.example.safemap.screen

sealed class Screen(val route: String) {
    object LoginScreen : Screen("loginscreen")
    object SignUpScreen : Screen("signupscreen")
    object MapScreen : Screen("mapscreen")

}