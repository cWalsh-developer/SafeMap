package com.example.safemap.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safemap.Model.LocationData
import com.example.safemap.R
import com.example.safemap.viewmodel.StreetlightViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

            val context = LocalContext.current
            val streetlightIcon = bitmapDescriptorFromPng(context, R.drawable.streetlight_image)

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
                    visible = true,
                    icon = streetlightIcon
                )
            }
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF3CB1FA))
        }
        error?.let {
            Log.e("LocationScreenView", "Error loading streetlights", it)
        }
    }
}

fun bitmapDescriptorFromPng(context: Context, @DrawableRes id: Int): BitmapDescriptor? {
    val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, id) ?: return null
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}