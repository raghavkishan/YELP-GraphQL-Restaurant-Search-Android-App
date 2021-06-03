package com.example.yelpapirestaurantsearch.viewModels

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yelpapirestaurantsearch.SearchRestaurantsQuery
import com.example.yelpapirestaurantsearch.models.ErrorResponse
import com.example.yelpapirestaurantsearch.models.RestaurantSearchQueryRequest
import com.example.yelpapirestaurantsearch.network.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * The [RestaurantsViewModel] is central view model associated withe the host activity.
 * It is used to keep state and update UI using live Data.
 */
class RestaurantsViewModel: ViewModel() {

    val location: MutableLiveData<Location> = MutableLiveData()
    val restaurantList: MutableLiveData<SearchRestaurantsQuery.Search> = MutableLiveData()
    val errorResponse: MutableLiveData<ErrorResponse> = MutableLiveData()
    var locationState: Location = Location("Location State")

    val selectedBusiness:
            MutableLiveData<SearchRestaurantsQuery.Business> = MutableLiveData()

    val titleBarTitle: MutableLiveData<String> = MutableLiveData()



    fun requestRestaurantInfo(restaurantSearchQueryRequest: RestaurantSearchQueryRequest,
                              authToken: String){

        viewModelScope.launch {
                val response = withContext(Dispatchers.IO) {
                    DataManager.getRestaurantsList(restaurantSearchQueryRequest, authToken)
            }

            if (response is SearchRestaurantsQuery.Search){
                restaurantList.value = response
            }else if(response is ErrorResponse) {
                errorResponse.value = response
            }
        }
    }



}