package com.example.safemap.View

import LocationScreenView
import LocationViewModel
import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import com.example.safemap.MainActivity
import com.example.safemap.model.LocationUtilities
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.safemap.model.Directions
import com.example.safemap.model.LocationData
import com.example.safemap.services.LocationService
import com.example.safemap.viewmodel.SettingsViewModel
import com.example.safemap.viewmodel.StreetlightViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen(
    settingsViewModel: SettingsViewModel,
    viewmodel: LocationViewModel,
    streetlightViewModel: StreetlightViewModel,
    destinationCoordinates: LatLng?,
    apiKey: String
)
{
    val context = LocalContext.current
    val locationUtilities = LocationUtilities(context)
    val location by remember { mutableStateOf(viewmodel.location)}
    val directions = remember { Directions(apiKey) }

    val requestPermissionPopup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    //All Permissions Granted
                    val serviceIntent = Intent(context, LocationService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else {
                    val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )

                    if (rationaleRequired) {
                        Toast.makeText(context, "Location Permission Required", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
    )
    LaunchedEffect(Unit) {
        if(!locationUtilities.hasLocationPermission(context))
        {
            //Permission Granted
            requestPermissionPopup.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))

        }
        else
        {
            viewmodel.registerReceiver(context)
            val serviceIntent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    if(location.value == null)
    {
        Text("Loading...")
    }
    else{
        LocationScreenView(
            destinationCoordinates = destinationCoordinates,
            location = location.value!!, onLocationSelected = {
            LocationData(it.latitude, it.longitude)
        },streetlightViewModel = streetlightViewModel, settingsViewModel = settingsViewModel,
            apiKey = apiKey,
            viewmodel = viewmodel)
    }
}

