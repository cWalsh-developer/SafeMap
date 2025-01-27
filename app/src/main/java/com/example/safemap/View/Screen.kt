package com.example.safemap.View

import androidx.annotation.DrawableRes
import com.example.safemap.R

sealed class Screen(val title: String, val route: String) {
    object LoginScreen : Screen("login","loginscreen")
    object SignUpScreen : Screen("signIn","signupscreen")
    object MapScreen : Screen("map","mapscreen")
    object MainView: Screen("Main","MainView")
    object LocationScreen: Screen("Location","LocationScreen")

    sealed class DrawerScreenHandler(val drawerTitle: String, val drawerRoute: String, @DrawableRes val icon: Int) : Screen(drawerTitle, drawerRoute){
        object TripPlanner : DrawerScreenHandler("Trip Planner", "trip_planner", R.drawable.ic_trip_planner)
        object FavouriteRoutes : DrawerScreenHandler("Favourite Routes", "favourite_routes", R.drawable.ic_favourite_routes)
        object EmergencyContact : DrawerScreenHandler("Emergency Contact", "emergency_contact", R.drawable.ic_emergency_contact)

    }
    sealed class BottomScreen(val bottomTitle: String, val bottomRoute: String, @DrawableRes val icon: Int) : Screen(bottomTitle, bottomRoute)
    {
        object MapScreen : BottomScreen("Map", "map", R.drawable.ic_map)
        object AccountScreen : BottomScreen("Account", "account", R.drawable.ic_account)
        object SettingsScreen : BottomScreen("Settings", "settings", R.drawable.ic_settings)
    }
}

//List of screens that are inside of the drawer
val screensInsideOfDrawer = listOf(
    Screen.DrawerScreenHandler.TripPlanner,
    Screen.DrawerScreenHandler.FavouriteRoutes,
    Screen.DrawerScreenHandler.EmergencyContact
)

val screensInBottom = listOf(
    Screen.BottomScreen.SettingsScreen,
    Screen.BottomScreen.MapScreen,
    Screen.BottomScreen.AccountScreen
)