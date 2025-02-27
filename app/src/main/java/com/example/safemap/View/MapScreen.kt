package com.example.safemap.View

import LocationScreenView
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import com.example.safemap.MainActivity
import com.example.safemap.model.LocationUtilities
import androidx.compose.ui.platform.LocalContext
import com.example.safemap.model.LocationData
import com.example.safemap.viewmodel.LocationViewModel
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
    val location = viewmodel.location.value

    val requestPermissionPopup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    //All Permissions Granted
                    locationUtilities.requestLocationUpdates(viewModel = viewmodel)
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
    if(location == null)
    {
        Text("Loading...")
        locationUtilities.requestLocationUpdates(viewModel = viewmodel)
    }
    else{
        LocationScreenView(
            destinationCoordinates = destinationCoordinates,
            location = location, onLocationSelected = {
            LocationData(it.latitude, it.longitude)
        },streetlightViewModel = streetlightViewModel, settingsViewModel = settingsViewModel,
            apiKey = apiKey)
    }
    LaunchedEffect(locationUtilities.hasLocationPermission(context)) {
        if(locationUtilities.hasLocationPermission(context))
        {
            //Permission Granted
            locationUtilities.requestLocationUpdates(viewModel = viewmodel)

        }
        else
        {
            requestPermissionPopup.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    }

