package com.example.safemap.model

data class UserAddresses(
    var addressLine1: String = "",
    var addressLine2: String = "",
    var townCity: String = "",
    var county: String = "",
    var country: String = "",
    var postCode: String = "",
    val userID: String = "")