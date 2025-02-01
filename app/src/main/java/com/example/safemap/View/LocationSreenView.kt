import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocationScreenView(
    location: LocationData,
    onLocationSelected: (LocationData) -> Unit,
    streetlightViewModel: StreetlightViewModel = viewModel()
) {
    val userLocation = remember { mutableStateOf(LatLng(location.latitude, location.longitude)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation.value, 12f)
    }

    val streetlights by streetlightViewModel.streetlights.collectAsState()
    val isLoading by streetlightViewModel.isLoading.collectAsState()
    val error by streetlightViewModel.error.collectAsState()

    val context = LocalContext.current
    var streetlightIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Load streetlights data
    LaunchedEffect(Unit) {
            streetlightIcon = bitmapDescriptorFromPng(context, R.drawable.streetlight_image)
            streetlightViewModel.loadStreetlights()
    }

    // Define the zoom threshold for rendering streetlights
    val minimumZoomForStreetlights = 14.7f

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                userLocation.value = it
            }
        ) {
            // User location marker
            Marker(state = MarkerState(position = userLocation.value))

            // Update selected location callback
            onLocationSelected(
                LocationData(
                    userLocation.value.latitude,
                    userLocation.value.longitude
                )
            )

            // Check if the zoom level is above the threshold
            if (cameraPositionState.position.zoom >= minimumZoomForStreetlights) {
                // Get visible bounds from camera state
                val visibleBounds = cameraPositionState.projection?.visibleRegion?.latLngBounds

                // Render markers only within visible bounds
                visibleBounds?.let { bounds ->
                    streetlightIcon?.let { icon ->
                        streetlights.filter { bounds.contains(LatLng(it.latitude, it.longitude)) }
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

        // Show loading indicator while data is being fetched
        if (isLoading && streetlights.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF26662a),
                trackColor = Color.LightGray
            )
        }

        // Log errors if any
        error?.let {
            Log.e("LocationScreenView", "Error loading streetlights", it)
        }
    }
}

suspend fun bitmapDescriptorFromPng(context: Context, @DrawableRes id: Int): BitmapDescriptor? {
    return withContext(Dispatchers.IO) {
        val bitmap = BitmapFactory.decodeResource(context.resources, id) ?: return@withContext null
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
