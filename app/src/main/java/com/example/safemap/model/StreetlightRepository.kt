package com.example.safemap.model

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class StreetlightRepository {
    private val baseUrl = "https://kingston.statmap.co.uk/map/wfs.svc/customer_platform_prod_wfs"

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    suspend fun getStreetlights(): List<Streetlight> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val url =
            URL("$baseUrl?service=WFS&request=GetFeature&srsname=EPSG:27700&typeName=statmap:alloy_electricalassets_no_faults&outputFormat=geojson&filter=%3CFilter%3E%3CAND%3E%3CPropertyIsEqualTo%3E%3CPropertyName%3ELEAFLET_STYLE%3C/PropertyName%3E%3CLiteral%3ESTREET_LIGHT%3C/Literal%3E%3C/PropertyIsEqualTo%3E%3CPropertyIsEqualTo%3E%3CPropertyName%3EBOROUGH%3C/PropertyName%3E%3CLiteral%3ERBK%3C/Literal%3E%3C/PropertyIsEqualTo%3E%3C/AND%3E%3C/Filter%3E")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            connection.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                parseGeoJson(response)
            }
        } catch (e: Exception) {
            Log.e("StreetlightRepository", "Error fetching streetlights", e)
            emptyList()
        } finally {
            connection.disconnect()
            _isLoading.value = false
        }
    }

    private fun parseGeoJson(geojson: String): List<Streetlight> {
        val gson = Gson()
        val jsonObject = gson.fromJson(geojson, JsonObject::class.java)
        val features = jsonObject.getAsJsonArray("features")
        val streetlights = mutableListOf<Streetlight>()

        for (feature in features) {
            val featureObject = feature.asJsonObject
            val geometry = featureObject.getAsJsonObject("geometry")
            if (geometry.get("type").asString == "Point") {
                val coordinates = geometry.getAsJsonArray("coordinates")
                val x = coordinates[0].asDouble
                val y = coordinates[1].asDouble

                // Convert from British National Grid (EPSG:27700) to WGS 84 (EPSG:4326)
                val (latitude, longitude) = convertToWgs84(x, y)

                val properties = featureObject.getAsJsonObject("properties")
                val id = properties.get("ID")?.asString ?: ""
                val leafletStyle = properties.get("LEAFLET_STYLE")?.asString ?: ""
                val borough = properties.get("BOROUGH")?.asString ?: ""

                streetlights.add(Streetlight(id, latitude, longitude, leafletStyle, borough))
            }
        }

        return streetlights
    }

    private fun convertToWgs84(x: Double, y: Double): Pair<Double, Double> {
        val crsFactory = CRSFactory()
        val ctFactory = CoordinateTransformFactory()

        // Define the source CRS (British National Grid - EPSG:27700)
        val sourceCRS = crsFactory.createFromParameters(
            "EPSG:27700",
            "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs"
        )

        // Define the target CRS (WGS 84 - EPSG:4326)
        val targetCRS = crsFactory.createFromParameters(
            "EPSG:4326",
            "+proj=longlat +datum=WGS84 +no_defs"
        )

        // Create the coordinate transform
        val transform = ctFactory.createTransform(sourceCRS, targetCRS)

        // Create the source coordinate
        val sourceCoord = ProjCoordinate(x, y)

        // Create the destination coordinate
        val destCoord = ProjCoordinate()

        // Transform the coordinate
        transform.transform(sourceCoord, destCoord)

        // Return the latitude and longitude
        return Pair(destCoord.y, destCoord.x)
    }

    private fun calculateStreetlightDensity(
        routePolyline: String, streetlights: List<Streetlight>,
        bufferRadiusMeters: Double = 50.0, segmentLengtheMeters: Double = 10.0
    ): List<Pair<LatLng, Double>> {
        val routePoints = PolylineUtils.decodePolyline(routePolyline)
        val segments = divideRouteIntoSegments(routePoints, segmentLengtheMeters)
        val streetlightDensity = mutableListOf<Pair<LatLng, Double>>()

        for (segment in segments) {
            val bufferCenter = segment.first
            val streetlightsInRadius = streetlights.count { streetlight ->
                isPointInCircle(
                    LatLng(
                        streetlight.latitude,
                        streetlight.longitude
                    ), bufferCenter, bufferRadiusMeters
                )
            }
            val segmentLength = calculateSegmentLength(segment)

            val density = if (segmentLength > 0) {
                streetlightsInRadius.toDouble() / segmentLength
            } else {
                0.0
            }

            val weight1 = 0.6
            val weight2 = 0.4
            val weightedDensity = (weight1 * density) + (weight2 * streetlightsInRadius)

            streetlightDensity.add(Pair(bufferCenter, weightedDensity))

        }
        return streetlightDensity
    }

    private fun calculateSegmentLength(segment: Pair<LatLng, LatLng>): Double {
        val segmentPoints = listOf(segment.first, segment.second)
        return SphericalUtil.computeLength(segmentPoints)
    }

    private fun divideRouteIntoSegments(
        routePoints: List<LatLng>,
        segmentLengthMeters: Double
    ): List<Pair<LatLng, LatLng>> {
        val segments = mutableListOf<Pair<LatLng, LatLng>>()
        for (i in 0 until routePoints.size - 1) {
            val startPoint = routePoints[i]
            val endPoint = routePoints[i + 1]
            val distance = calculateDistance(startPoint, endPoint)
            val numSegments = (distance / segmentLengthMeters).toInt()

            if (numSegments > 0) {
                for (j in 0 until numSegments) {
                    val fraction = (j + 1).toDouble() / (numSegments).toDouble()
                    val intermediatePoint = interpolatePoint(startPoint, endPoint, fraction)
                    segments.add(Pair(startPoint, intermediatePoint))
                }
            } else {
                segments.add(Pair(startPoint, endPoint))

            }
        }
        return segments
    }

    private fun isPointInCircle(
        point: LatLng,
        circleCenter: LatLng,
        radiusMeters: Double
    ): Boolean {
        val distance = calculateDistance(point, circleCenter)
        return distance <= radiusMeters
    }

    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0
        val lat1Rad = Math.toRadians(point1.latitude)
        val lon1Rad = Math.toRadians(point1.longitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val lon2Rad = Math.toRadians(point2.longitude)

        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) *
                cos(lat2Rad) *
                sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun interpolatePoint(start: LatLng, end: LatLng, fraction: Double): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lng = start.longitude + (end.longitude - start.longitude) * fraction
        return LatLng(lat, lng)
    }

    fun chooseBestRoute(
        routes: List<String>,
        streetlights: List<Streetlight>,
        bufferRadiusMeters: Double = 50.0,
        segmentLengthMeters: Double = 10.0
    ): String {
        var bestRoute = ""
        var bestDensity = Double.MAX_VALUE

        for (routePolyline in routes) {
            val segmentDensities = calculateStreetlightDensity(
                routePolyline,
                streetlights,
                bufferRadiusMeters,
                segmentLengthMeters
            )
            val routePenalty = calculateRoutePenalty(segmentDensities)

            if (routePenalty < bestDensity) {
                bestDensity = routePenalty
                bestRoute = routePolyline
            }
        }
        return bestRoute
    }

    private fun calculateRoutePenalty(segmentDensities: List<Pair<LatLng, Double>>): Double {
        var totalPenalty = 0.0

        for ((_, density) in segmentDensities) {
            val k = 5.0
            val segmentPenalty = 100 * exp(-density * k)
            totalPenalty += segmentPenalty
        }
        return totalPenalty
    }
}