package com.example.safemap.Model

data class Streetlight(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val leafletStyle: String,
    val borough: String,
)
