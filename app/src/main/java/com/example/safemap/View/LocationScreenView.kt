import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safemap.model.LocationData
import com.example.safemap.R
import com.example.safemap.model.Directions
import com.example.safemap.model.LocationUtilities
import com.example.safemap.viewmodel.SettingsViewModel
import com.example.safemap.viewmodel.StreetlightViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.model.DirectionsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val DEFAULT_BUFFER_RADIUS = 50.0
const val DEFAULT_SEGMENT_LENGTH = 10.0

@Composable
fun LocationScreenView(
    destinationCoordinates: LatLng?,
    location: LocationData,
    onLocationSelected: (LatLng) -> Unit,
    streetlightViewModel: StreetlightViewModel = viewModel(),
    settingsViewModel: SettingsViewModel,
    apiKey: String,
    viewmodel : LocationViewModel
) {
    var userLocation by remember { mutableStateOf(LatLng(location.latitude, location.longitude)) }
    val streetlightEnabled by settingsViewModel.isStreetlightEnabled
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(userLocation!!.latitude, userLocation.longitude), 12f)
    }
    val userRouteLocation = LatLng(userLocation.latitude, userLocation.longitude)

    val streetlights by streetlightViewModel.streetlights.collectAsState()
    val haveStreetlightsLoaded by remember {derivedStateOf { streetlights.isNotEmpty()}}
    val directions = remember { Directions(apiKey) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>?>(null) }
    var directionsResult by remember { mutableStateOf<DirectionsResult?>(null) }

    val context = LocalContext.current
    var streetlightIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var userLocationIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    val locationUtilities = remember { LocationUtilities(context) }

    // Load streetlights data
    LaunchedEffect(Unit) {
        if (streetlightEnabled) {
            streetlightIcon = bitmapDescriptorFromPng(context, R.drawable.streetlight_image)
            streetlightViewModel.loadStreetlights()
        }
            userLocationIcon = bitmapDescriptorFromVector(context, R.drawable.icon_location)
        locationUtilities.startLocationTracking()
    }

    DisposableEffect(Unit) {
        onDispose {
            locationUtilities.stopLocationUpdates()
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
                userLocation = LatLng(it.latitude, it.longitude)
            },

            ) {
            // User location marker
            userLocation.let {
                Marker(state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = "Your Location",
                    snippet = "You are here",
                    icon = userLocationIcon)
            }
            userLocation = LatLng(location.latitude, location.longitude)

            val bufferRadiusMeters = DEFAULT_BUFFER_RADIUS
            val segmentLengthMeters = DEFAULT_SEGMENT_LENGTH

            LaunchedEffect(destinationCoordinates , haveStreetlightsLoaded, userLocation) {
                userLocation.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    if (destinationCoordinates != null) {
                        directions.checkUserLocationForTurn(userLatLng, context, destinationCoordinates)
                    }
                }
                if (destinationCoordinates != null) {
                    if (streetlightEnabled && streetlights.isNotEmpty()) {
                        Log.d("LocationScreenView", "Streetlight size: ${streetlights.size}")
                        directions.getWalkingDirections(
                            userRouteLocation,
                            destinationCoordinates,
                            streetlights,
                            context,
                            bufferRadiusMeters,
                            segmentLengthMeters,
                        ) { result, polyline ->
                            directionsResult = result
                            polylinePoints = polyline
                        }
                    } else {
                        directions.getWalkingDirections(
                            userRouteLocation,
                            destinationCoordinates,
                            emptyList(),
                            context,
                            bufferRadiusMeters,
                            segmentLengthMeters
                        ) { result, polyline ->
                            directionsResult = result
                            polylinePoints = polyline
                        }
                    }
                }
            }
                if (destinationCoordinates != null) {
                    Marker(state = MarkerState(position = destinationCoordinates))
                }
                Polyline(points = polylinePoints?: emptyList(), color = Color.Red, width = 7f)



            // Update selected location callback
            userLocation.let {
                onLocationSelected(LatLng(it.latitude, it.longitude))
            }

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

suspend fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
    return withContext(Dispatchers.IO) {
        // Retrieve the drawable from resources
        val vectorDrawable: Drawable = ContextCompat.getDrawable(context, vectorResId)
            ?: return@withContext null

        // Set the bounds for the drawable
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

        // Create a bitmap with the same dimensions as the drawable
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // Create a canvas to draw on the bitmap
        val canvas = Canvas(bitmap)

        // Draw the drawable onto the canvas
        vectorDrawable.draw(canvas)

        // Create a BitmapDescriptor from the bitmap
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
