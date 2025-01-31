package com.example.safemap.Model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import java.net.HttpURLConnection
import java.net.URL

class StreetlightRepository {
    private val baseUrl = "https://kingston.statmap.co.uk/map/wfs.svc/customer_platform_prod_wfs"

    suspend fun getStreetlights(): List<Streetlight> = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl?service=WFS&request=GetFeature&srsname=EPSG:27700&typeName=statmap:alloy_electricalassets_no_faults&outputFormat=geojson&filter=%3CFilter%3E%3CAND%3E%3CPropertyIsEqualTo%3E%3CPropertyName%3ELEAFLET_STYLE%3C/PropertyName%3E%3CLiteral%3ESTREET_LIGHT%3C/Literal%3E%3C/PropertyIsEqualTo%3E%3CPropertyIsEqualTo%3E%3CPropertyName%3EBOROUGH%3C/PropertyName%3E%3CLiteral%3ERBK%3C/Literal%3E%3C/PropertyIsEqualTo%3E%3C/AND%3E%3C/Filter%3E")
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
}