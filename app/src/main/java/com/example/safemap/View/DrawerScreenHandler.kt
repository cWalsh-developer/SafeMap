package com.example.safemap.View

import androidx.annotation.DrawableRes
import com.example.safemap.R

sealed class DrawerScreenHandler(val title: String, val route: String, @DrawableRes val icon: Int) {
    object TripPlanner : DrawerScreenHandler("Trip Planner", "trip_planner", R.drawable.ic_trip_planner)
    object FavouriteRoutes : DrawerScreenHandler("Favourite Routes", "favourite_routes", R.drawable.ic_favourite_routes)
    object EmergencyContact : DrawerScreenHandler("Emergency Contact", "emergency_contact", R.drawable.ic_emergency_contact)

}
//List of screens that are inside of the drawer
val screensInsideOfDrawer = listOf(
    DrawerScreenHandler.TripPlanner,
    DrawerScreenHandler.FavouriteRoutes,
    DrawerScreenHandler.EmergencyContact
)