package com.example.safemap.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.safemap.Model.LocationData
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationScreenView(
    location: LocationData,
    onLocationSelected: (LocationData) -> Unit
)
{
    val userLocation = remember{
        mutableStateOf(LatLng(location.latitude, location.longitude))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation.value, 10f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(modifier = Modifier.weight(1f).fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                userLocation.value = it
            }){
            Marker(state = MarkerState(position = userLocation.value))

            val newLocation = LocationData(userLocation.value.latitude, userLocation.value.longitude)
            onLocationSelected(newLocation)
        }
    }
}