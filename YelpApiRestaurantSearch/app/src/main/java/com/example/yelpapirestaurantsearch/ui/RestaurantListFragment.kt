package com.example.yelpapirestaurantsearch.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.system.Os.accept
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yelpapirestaurantsearch.R
import com.example.yelpapirestaurantsearch.SearchRestaurantsQuery
import com.example.yelpapirestaurantsearch.models.RestaurantSearchQueryRequest
import com.example.yelpapirestaurantsearch.network.model.SelectedBusiness
import com.example.yelpapirestaurantsearch.viewModels.RestaurantsViewModel
import com.google.android.material.progressindicator.CircularProgressIndicator

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 *
 * This projects uses the MVVM design pattern to architect the app thus seperating the concerns
 * and making the data layer more independent from the UI layer
 *
 * We use the Android View Model, Navigation and Live Data libraries (Jetpack libraries)
 */

/**
 *
 * A simple [Fragment] subclass.
 * [RestaurantListFragment] requests for location permission if its not already provided.
 * All the data requests and data is stored and used from the [RestaurantsViewModel]
 */
class RestaurantListFragment : RestaurantBaseFragment(), LocationListener {

    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 99999
    private val logTAG = "RestaurantListFragment"
    lateinit var restaurantViewModel: RestaurantsViewModel
    private var restaurantListRecyclerView : RecyclerView? = null
    private var restaurantProgressSpinner: ProgressBar? = null
    var navController: NavController? = null
    var sharedPreference : SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            restaurantViewModel = ViewModelProviders.of(it).get(RestaurantsViewModel::class.java)
            sharedPreference = it.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance( ) = RestaurantListFragment()
        val REQUEST_LOCATION_FIRST_TIME: String = "request location first time share pref string"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater
            .inflate(R.layout.fragment_restaurant_list, container, false)

        restaurantListRecyclerView = fragmentView
            .findViewById<RecyclerView>(R.id.restaurant_list_recycler_view);

        restaurantProgressSpinner = fragmentView
            .findViewById<ProgressBar>(R.id.restaurant_list_progress_spinner)


        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
    }



    override fun onResume() {
        super.onResume()

        restaurantViewModel.titleBarTitle.value = "Burrito Places"

        getLocation()
        displayRestaurants()

        restaurantViewModel.location.observe( this, Observer {
            Log.v(logTAG, "Location updated in observer ${it.longitude} + ${it.latitude}")

            if (restaurantViewModel.locationState.latitude != it.latitude
                || restaurantViewModel.locationState.longitude != it.longitude) {

                restaurantViewModel.locationState = it
                restaurantViewModel.requestRestaurantInfo(
                    RestaurantSearchQueryRequest(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        radius = 19312.1,
                        category = "mexican",
                        term = "Burrito"
                    ),
                    "xYh_wneIRRUphFKur-qWaczG-kjrT-gE7dvV-ViWUCs6yz6J2DGgYUowZp_AovaW6PD--vlUT9a1iHk62xjc2nNsaUD-68NZ7XdhWOVHQZ4BA5qORMX_tmr6V6mpYHYx"
                )
            }
        })

    }

    /**
     * Location & Location permission
     */

    private fun getLocation(){

        if (ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            val locationManager = activity
                    ?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if(sharedPreference != null
                && sharedPreference?.getBoolean(REQUEST_LOCATION_FIRST_TIME, true)!!){
                var editor = sharedPreference?.edit()
                editor?.putBoolean(REQUEST_LOCATION_FIRST_TIME,false)
                editor?.commit()

                restaurantViewModel.location.value =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                Long.MAX_VALUE,
                    1f,
                    this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                Long.MAX_VALUE,
                1f,
                this)

//            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }else{
            getLocationPermission()
        }
    }


    fun getLocationPermission(){

        displayAlertDialog(
            title = "Allow Burrito Places to access your location?",
            message = "This app collects location data to provide you a " +
                    "list of all restaurants that serve Burritos withing a 12 mile radius",
            positiveButtonTitle = getString(android.R.string.ok),
            positiveClickCallback = ::locationInfoPositiveCallback,
            negativeButtonTitle = getString(android.R.string.cancel),
            negativeButtonCallback = ::locationInfoNegativeCallback,
            extraData = null
        )
    }

    private fun locationInfoPositiveCallback(extraData: Any?){
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun locationInfoNegativeCallback(extraData: Any?){
        displayAlertDialog(
            title = "Location Permission required",
            message = "This app requires your location to show near by burrito restaurants",
            positiveButtonTitle = "Ok",
            positiveClickCallback = ::locationPermissionNotProvided
        )
    }

    private fun locationPermissionNotProvided(){
    }

    override fun onLocationChanged(p0: Location) {
        Log.v(logTAG, "Location updated ${p0.longitude} + ${p0.latitude}")
        restaurantViewModel.location.value = p0
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(),
                    "Permission Granted", Toast.LENGTH_SHORT).show()
                Log.v(logTAG, "Location Permission Granted")
                getLocation()
            }
            else {
                Toast.makeText(requireContext(),
                    "Permission Denied", Toast.LENGTH_SHORT).show()
                Log.v(logTAG, "Location Permission Denied")
                // Show another pop up to ask again and then close the application.
            }
        }
    }

    /**
     * Display Restaurants
     */

    private fun displayRestaurants(){

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(
                this.requireContext(), LinearLayoutManager.VERTICAL, false)
        restaurantListRecyclerView?.layoutManager = layoutManager

        val listAdapter: RestaurantRecyclerViewAdapter =
            RestaurantRecyclerViewAdapter( ArrayList<SearchRestaurantsQuery.Business>())

        restaurantListRecyclerView?.adapter = listAdapter

        listAdapter.onItemClicked = { business, restaurantAddressView->

            val extras = FragmentNavigatorExtras(
                    restaurantAddressView to business.id!!
            )
            val action = RestaurantListFragmentDirections.actionRestaurantListFragmentToRestaurantDetailsFragment(address = business.location?.address1!!)

            restaurantViewModel.selectedBusiness.value = business

//            navController!!.navigate(
//                R.id.action_restaurantListFragment_to_restaurantDetailsFragment)

            navController!!.navigate(action, extras)

        }

        restaurantViewModel.restaurantList.observe(this, Observer {

            if (!it.business.isNullOrEmpty()) {
                restaurantProgressSpinner?.visibility = View.GONE
                listAdapter.setData(it.business)
            }
        })

        restaurantViewModel.errorResponse.observe(this, Observer {
            displayAlertDialog(title = it.errorTitle,
            message = it.errorMessage,
            positiveButtonTitle = "Ok",
            positiveClickCallback = ::ErrorResponsePositiveCallback)

        })
    }

    private fun ErrorResponsePositiveCallback(){

    }

}