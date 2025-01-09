package com.example.safemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safemap.screen.LoginScreen
import com.example.safemap.screen.Screen
import com.example.safemap.screen.SignUpScreen
import com.example.safemap.ui.theme.SafeMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            SafeMapTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background)
                {
                    NavigationManager(navController)
                }
            }
        }
    }
}

@Composable
fun NavigationManager(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.LoginScreen.route)
    {
        composable(Screen.SignUpScreen.route)
        {

            SignUpScreen(onNavigateToSignIn = { navController.navigate(Screen.LoginScreen.route) })
        }
        composable(Screen.LoginScreen.route)
        {
            LoginScreen(onNavigateToSignUp = { navController.navigate(Screen.SignUpScreen.route) })
        }

    }
}