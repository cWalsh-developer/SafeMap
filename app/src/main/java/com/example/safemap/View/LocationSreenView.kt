import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safemap.model.LocationData
import com.example.safemap.R
import com.example.safemap.model.Directions
import com.example.safemap.viewmodel.SettingsViewModel
import com.example.safemap.viewmodel.StreetlightViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.model.DirectionsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocationScreenView(
    destinationCoordinates: LatLng?,
    location: LocationData,
    onLocationSelected: (LatLng) -> Unit,
    streetlightViewModel: StreetlightViewModel = viewModel(),
    settingsViewModel: SettingsViewModel,
    apiKey: String
) {
    val userLocation = remember { mutableStateOf(LatLng(location.latitude, location.longitude)) }
    val streetlightEnabled by settingsViewModel.isStreetlightEnabled
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation.value, 12f)
    }
    val userRouteLocation = LatLng(userLocation.value.latitude, userLocation.value.longitude)

    val streetlights by streetlightViewModel.streetlights.collectAsState()
    val directions = remember { Directions(apiKey) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>?>(null) }
    var directionsResult by remember { mutableStateOf<DirectionsResult?>(null) }

    val context = LocalContext.current
    var streetlightIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Load streetlights data
    LaunchedEffect(Unit) {
        if (streetlightEnabled) {
            streetlightIcon = bitmapDescriptorFromPng(context, R.drawable.streetlight_image)
            streetlightViewModel.loadStreetlights()
        }
    }

    // Define the zoom threshold for rendering streetlights
    val minimumZoomForStreetlights = 16.0f
    Box(modifier = Modifier.fillMaxSize())
    {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                userLocation.value = it
            },

            ) {
            // User location marker
            Marker(state = MarkerState(position = userLocation.value), snippet = "You are here")

            if (destinationCoordinates != null) {
                Marker(state = MarkerState(position = destinationCoordinates))
                directions.getWalkingDirections(
                    userRouteLocation,
                    destinationCoordinates
                ) { result, polyline ->
                    directionsResult = result
                    polylinePoints = polyline
                    Log.d("PolylinePoints", "Polyline Result: $polylinePoints")
                }
            }
            Polyline(points = polylinePoints ?: emptyList(), color = Color.Red, width = 7f)


            // Update selected location callback
            onLocationSelected(
                LatLng(
                    userLocation.value.latitude,
                    userLocation.value.longitude
                )
            )
            if (streetlightEnabled) {
                // Check if the zoom level is above the threshold
                if (cameraPositionState.position.zoom >= minimumZoomForStreetlights) {
                    // Get visible bounds from camera state
                    val visibleBounds = cameraPositionState.projection?.visibleRegion?.latLngBounds

                    // Render markers only within visible bounds
                    visibleBounds?.let { bounds ->
                        streetlightIcon?.let { icon ->
                            streetlights.filter {
                                bounds.contains(
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    )
                                )
                            }
                                .forEach { streetlight ->
                                    Marker(
                                        state = MarkerState(
                                            position = LatLng(
                                                streetlight.latitude,
                                                streetlight.longitude
                                            )
                                        ),
                                        title = streetlight.id,
                                        snippet = "Leaflet Style ${streetlight.leafletStyle}, Borough: ${streetlight.borough}",
                                        icon = icon
                                    )
                                }
                        }
                    }
                }

            }
        }
    }
}

suspend fun bitmapDescriptorFromPng(context: Context, @DrawableRes id: Int): BitmapDescriptor? {
    return withContext(Dispatchers.IO) {
        val bitmap = BitmapFactory.decodeResource(context.resources, id) ?: return@withContext null
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
