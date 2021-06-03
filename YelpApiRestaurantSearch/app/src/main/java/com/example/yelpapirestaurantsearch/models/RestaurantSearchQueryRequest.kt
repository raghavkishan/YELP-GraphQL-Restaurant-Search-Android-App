package com.example.yelpapirestaurantsearch.models

data class RestaurantSearchQueryRequest(
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val category: String,
    val term: String?
)
