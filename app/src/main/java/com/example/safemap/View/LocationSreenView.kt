package com.example.safemap.View

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safemap.Model.LocationData
import com.example.safemap.Model.LocationUtilities
import com.example.safemap.viewmodel.StreetlightViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationScreenView(
    location: LocationData,
    onLocationSelected: (LocationData) -> Unit,
    streetlightViewModel: StreetlightViewModel = viewModel()
)
{
    val userLocation = remember{
        mutableStateOf(LatLng(location.latitude, location.longitude))
    }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(userLocation.value, 10f)
        }

    val streetlights by streetlightViewModel.streetlights.collectAsState()
    val isLoading by streetlightViewModel.isLoading.collectAsState()
    val error by streetlightViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        streetlightViewModel.loadStreetlights()
    }

        Box(modifier = Modifier.fillMaxSize()) {

            GoogleMap(modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = {
                    userLocation.value = it
                }){
                Marker(state = MarkerState(position = userLocation.value))
                val newLocation = LocationData(userLocation.value.latitude, userLocation.value.longitude)
                onLocationSelected(newLocation)

                if (isLoading) {
                    Log.d("Streetlight Loading", "Loading...")
                } else if (error != null) {
                    Log.d("Streetlight Error", "Error: Failed to display Streelight Locations")
                }
                else {
                    streetlights.forEach { streetlight ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    streetlight.latitude,
                                    streetlight.longitude
                                )
                            ),
                            title = streetlight.id,
                            snippet = "Leaflet Style ${streetlight.leafletStyle}, Borough: ${streetlight.borough}",
                            visible = true
                        )
                    }
                }
            }
        }
    }