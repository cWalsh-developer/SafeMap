package com.example.safemap.Model

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.safemap.viewmodel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationUtilities(val context: Context) {

    private val _fusedLocationClient: FusedLocationProviderClient
    = LocationServices.getFusedLocationProviderClient(context)

    //Suppress missing permissions error because it is handled elsewhere
@SuppressLint("MissingPermission")
    fun requestLocationUpdates(viewModel: LocationViewModel)
    {
        val locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult)
            {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    val newLocation = LocationData(location.latitude, location.longitude)
                    viewModel.updateLocation(newLocation)
                }
            }
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        _fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun hasLocationPermission(context: Context): Boolean
    {
        return ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }
}