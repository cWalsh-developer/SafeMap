package com.example.safemap.model

import android.util.Log
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.GeocodingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Geocoder(private val apiKey: String) {
    private val geoApiContent = GeoApiContext.Builder().apiKey(apiKey)
        .build()

    fun geocodeAddress(address: String, onResult: (GeocodingResult?) -> Unit)
    {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = GeocodingApi.geocode(geoApiContent, address)
                val results = request.await().firstOrNull()
                withContext(Dispatchers.Main)
                {
                    onResult(results)
                }
            }
            catch (e: Exception)
            {
                Log.d("Geocoder", "Error geocoding address: ${address}")
                withContext(Dispatchers.Main)
                {
                    onResult(null)
                }
            }
            }
        }
}