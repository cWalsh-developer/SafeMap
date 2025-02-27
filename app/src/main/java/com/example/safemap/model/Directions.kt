package com.example.safemap.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Directions(private val apiKey: String) {
    private val geoApiContent = GeoApiContext.Builder()
        .apiKey(apiKey).build()

    fun getWalkingDirections(origin: LatLng,
                             destination: LatLng,
                             streetlights: List<Streetlight>,
                             bufferedRadiusMeters: Double = 50.0,
                             segmentLengthMeters: Double = 10.0,
                             onResult: (DirectionsResult?, List<LatLng>?) -> Unit)
    {
        Log.d("Directions", "getWalkingDirections called - Streetlight Enabled: ${streetlights.isNotEmpty()}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request: DirectionsApiRequest = DirectionsApi.newRequest(geoApiContent)
                    .origin("${origin.latitude},${origin.longitude}")
                    .destination("${destination.latitude},${destination.longitude}")
                    .mode(TravelMode.WALKING).alternatives(true)

                val result: DirectionsResult = request.await()
                val routes = result.routes.map { it.overviewPolyline.encodedPath }
                val bestRoutePolyline = StreetlightRepository().chooseBestRoute(routes, streetlights,
                    bufferedRadiusMeters, segmentLengthMeters)
                val bestRoute = result.routes.find { it.overviewPolyline.encodedPath == bestRoutePolyline }
                val polyline = if (bestRoute != null) {
                    PolylineUtils.decodePolyline(bestRoute.overviewPolyline.encodedPath)
                } else {
                    emptyList()
                }
                withContext(Dispatchers.Main)
                {
                    onResult(result, polyline)
                }
            }
            catch (e: Exception)
            {
                Log.d("Directions", "Error getting walking directions", e)
                withContext(Dispatchers.Main)
                {
                    onResult(null, null)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateETA(directionsResult: DirectionsResult?): String {
        if (directionsResult == null || directionsResult.routes.isEmpty()) {
            return "No directions available"
        }

        val duration = directionsResult.routes[0].legs[0].duration
        val currentTime = LocalDateTime.now()
        val eta = currentTime.plusSeconds(duration.inSeconds)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        return "ETA ${eta.format(formatter)}"

    }


}