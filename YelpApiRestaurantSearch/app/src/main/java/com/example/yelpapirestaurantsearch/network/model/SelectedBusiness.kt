package com.example.yelpapirestaurantsearch.network.model

import com.example.yelpapirestaurantsearch.SearchRestaurantsQuery

data class SelectedBusiness(

    val business: SearchRestaurantsQuery.Business,
    val transitionStrings: HashMap<String, String>
)
