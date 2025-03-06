package com.example.safemap.model

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
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
import kotlin.math.atan2
import kotlin.math.sqrt

class Directions(private val apiKey: String) {
    private val geoApiContent = GeoApiContext.Builder()
        .apiKey(apiKey).build()
    private var routePolyline: List<LatLng> = emptyList()
    private var lastReroute: Long = 0

    fun getWalkingDirections(origin: LatLng,
                             destination: LatLng,
                             streetlights: List<Streetlight>,
                             context: Context,
                             bufferedRadiusMeters: Double = 50.0,
                             segmentLengthMeters: Double = 10.0,
                             onResult: (DirectionsResult?, List<LatLng>?) -> Unit)
    {
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

                routePolyline = bestRoute?.let{PolylineUtils.decodePolyline(it.overviewPolyline.encodedPath)} ?: emptyList()
                withContext(Dispatchers.Main)
                {
                    onResult(result, routePolyline)
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

    private var lastVibratedTurnIndex: Int? = null
    private var lastVibrationTime: Long = 0
    private val vibrationCooldownMillis = 10000


    fun checkUserLocationForTurn(currentLocation: LatLng, context: Context, destination: LatLng) {
        if (routePolyline.isEmpty()) {
            Log.d("Directions", "No route polyline available")
            return
        }

        val closestPointIndex = findClosestPointOnRoute(currentLocation, routePolyline)

        if (closestPointIndex in 1 until routePolyline.size - 1) {
            val prevPoint = routePolyline[closestPointIndex - 1]
            val nextPoint = routePolyline[closestPointIndex + 1]

            val angle = calculateAngle(prevPoint, currentLocation, nextPoint)
            val distanceToTurn = calculateDistance(currentLocation, nextPoint)

            Log.d("Directions", "Angle: $angle, Distance to Next Turn: $distanceToTurn")

            // Ensure user is within turn detection range
            if (calculateDistance(currentLocation, nextPoint) <= distanceToTurn) {
                val currentTime = System.currentTimeMillis()

                // 🔹 Prevent duplicate vibrations within the cooldown period
                if (lastVibratedTurnIndex != closestPointIndex || (currentTime - lastVibrationTime) > vibrationCooldownMillis) {
                    when {
                        angle >= 45 -> {
                            vibrateRight(context)
                            Log.d("Directions", "Right Turn detected at index $closestPointIndex (Angle: $angle)")
                        }
                        angle <= -45 -> {
                            vibrateLeft(context)
                            Log.d("Directions", "Left Turn detected at index $closestPointIndex (Angle: $angle)")
                        }
                    }
                    lastVibratedTurnIndex = closestPointIndex
                    lastVibrationTime = currentTime // Update last vibration time
                } else {
                    Log.d("Directions", "Skipping duplicate vibration at index $closestPointIndex (Cooldown Active)")
                }
            }
        }
        //Call reroute() only if user moves too far (100m) from intended path
        val distanceToRoute = calculateDistance(currentLocation, routePolyline[closestPointIndex])
        if (distanceToRoute > 100) { // Adjust rerouting sensitivity
            reroute(currentLocation, destination, context)
        }
    }

    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters

        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(p1.latitude)) * kotlin.math.cos(Math.toRadians(p2.latitude)) *
                kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)

        return 2 * earthRadius * atan2(sqrt(a), sqrt(1 - a)) // Returns distance in meters
    }




//    private fun detectRouteAndVibrate(polyline: List<LatLng>, context: Context) {
//        Log.d("DetectRouteVibrate", "Checking polyline for turns: ${polyline.size} points")
//
//        if (polyline.size < 3) {
//            Log.d("DetectRouteVibrate", "Not enough points to detect turns.")
//            return
//        }
//
//        for (i in 1 until polyline.size - 1) {
//            val angle = calculateAngle(polyline[i - 1], polyline[i], polyline[i + 1])
//
//            // Only vibrate if within turn distance
//            if (calculateDistance(polyline[i], polyline[i + 1]) <= turnDetectionDistance) {
//                if (lastVibratedTurnIndex != i) { //Prevents accidental double vibrations
//                    when {
//                        angle > minTurnAngle -> {
//                            vibrateRight(context)
//                            Log.d("DetectRouteVibrate", "Right Turn at index $i")
//                        }
//                        angle < -minTurnAngle -> {
//                            vibrateLeft(context)
//                            Log.d("DetectRouteVibrate", "Left Turn at index $i")
//                        }
//                    }
//                    lastVibratedTurnIndex = i
//                }
//            }
//        }
//    }

    private fun reroute(currentLocation: LatLng, destination: LatLng, context: Context) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastReroute < 15000) return // Prevent frequent rerouting

        val closestPointIndex = findClosestPointOnRoute(currentLocation, routePolyline)
        val distanceToRoute = calculateDistance(currentLocation, routePolyline[closestPointIndex])

        val nextPointIndex = (closestPointIndex + 1).coerceAtMost(routePolyline.size - 1)
        val expectedDirection = calculateAngle(routePolyline[closestPointIndex], routePolyline[nextPointIndex], destination)
        val userDirection = calculateAngle(routePolyline[closestPointIndex], currentLocation, destination)

        // Reroute only if user is far from the route AND moving in the wrong direction
        if (distanceToRoute > 100 && Math.abs(expectedDirection - userDirection) > 45) {
            lastReroute = currentTime
            Log.d("Reroute", "User off-course ($distanceToRoute m) and moving in wrong direction, rerouting...")

            getWalkingDirections(currentLocation, destination, emptyList(), context, bufferedRadiusMeters = 50.0, segmentLengthMeters = 10.0) { result, polyline ->
                if (polyline != null) {
                    routePolyline = polyline
                    Log.d("Reroute", "New route calculated")
                }
            }
        }
    }


    private fun findClosestPointOnRoute(current: LatLng, route: List<LatLng>): Int {
        var closestIndex = 0
        var minDistance = Double.MAX_VALUE

        for (i in route.indices) {
            val distance = calculateDistance(current, route[i])
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = i
            }
        }
        return closestIndex
    }

    private fun calculateAngle(p1: LatLng, p2: LatLng, p3: LatLng): Double
    {
        val angle1 = Math.toDegrees(atan2(p2.longitude - p1.longitude, p2.latitude - p1.latitude))
        val angle2 = Math.toDegrees(atan2(p3.longitude - p2.longitude, p3.latitude - p2.latitude))
        var angle = angle2 - angle1

        if (angle > 180) angle -= 360
        if (angle < -180) angle += 360
        return angle
    }

    private fun vibrate(context: Context, pattern: LongArray)
    {
        val vibrator = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        }
        else
        {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if(vibrator.hasVibrator())
        {
            val effect = android.os.VibrationEffect.createWaveform(pattern, -1)
            vibrator.vibrate(effect)
        }

    }

    private fun vibrateRight(context: Context)
    {
        val pattern = longArrayOf(0, 400) // Vibrate for 400ms (one long vibration)
        vibrate(context, pattern)
        Log.d("Vibrate", "Right Vibrate")

    }

    private fun vibrateLeft(context: Context)
    {
        val pattern = longArrayOf(0, 200, 200, 200) // Vibrate for 200ms, pause for 200ms, repeat
        vibrate(context, pattern)
        Log.d("Vibrate", "Left Vibrate")
    }

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