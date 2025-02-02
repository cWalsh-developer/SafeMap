package com.example.safemap

import LocationScreenView
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safemap.Model.LocationData
import com.example.safemap.Model.LocationUtilities
import com.example.safemap.View.MainView
import com.example.safemap.View.LoginScreen
import com.example.safemap.View.MapScreen
import com.example.safemap.View.Screen
import com.example.safemap.View.SignUpScreen
import com.example.safemap.ui.theme.SafeMapTheme
import com.example.safemap.viewmodel.AuthoriseViewModel
import com.example.safemap.Model.Result
import com.example.safemap.Model.StreetlightRepository
import com.example.safemap.View.SettingsScreen
import com.example.safemap.viewmodel.LocationViewModel
import com.example.safemap.viewmodel.StreetlightViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val authoriseViewModel: AuthoriseViewModel = viewModel()
            val locationViewModel: LocationViewModel = viewModel()
            val locationUtilities = LocationUtilities(this)

                SafeMapTheme() {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background)
                {
                    NavigationManager(navController, authoriseViewModel, locationViewModel, locationUtilities)
                }
            }
        }
    }
}

@Composable
fun NavigationManager(navController: NavHostController, authoriseViewModel: AuthoriseViewModel, locationViewModel: LocationViewModel,
                      locationUtilities: LocationUtilities) {
    NavHost(
        navController, startDestination =
            if(AuthoriseViewModel().checkStatus() == Result.Success(true))
            {
                Screen.MainView.route
            }
            else
            {
                Screen.LoginScreen.route
            }
    )
    {
        composable(Screen.SignUpScreen.route)
        {

            SignUpScreen(authoriseViewModel = authoriseViewModel,onNavigateToSignIn = {
                navController.navigate(Screen.LoginScreen.route) })
        }
        composable(Screen.LocationScreen.route)
        {
            LocationScreenView(location = locationViewModel.location.value!!, onLocationSelected = {
                LocationData(locationViewModel.location.value!!.latitude, locationViewModel.location.value!!.longitude)
            }, streetlightViewModel = StreetlightViewModel(streetlightRepository = StreetlightRepository()), settingsViewModel = viewModel())
        }
        composable(Screen.LoginScreen.route)
        {
            LoginScreen(authoriseViewModel = authoriseViewModel,
                onSignInSuccess = { navController.navigate(Screen.MainView.route) },
                onNavigateToSignUp = { navController.navigate(Screen.SignUpScreen.route) })
        }
        composable(Screen.MapScreen.route)
        {
            MapScreen(
                viewmodel = LocationViewModel(),
                streetlightViewModel = StreetlightViewModel(streetlightRepository = StreetlightRepository()),
                settingsViewModel = viewModel()
            )
        }
        composable(Screen.MainView.route)
        {
            MainView(locationViewModel = locationViewModel, authoriseViewModel = authoriseViewModel, onNavigateToSignIn = {
                navController.navigate(Screen.LoginScreen.route)})
            }
        }


    }