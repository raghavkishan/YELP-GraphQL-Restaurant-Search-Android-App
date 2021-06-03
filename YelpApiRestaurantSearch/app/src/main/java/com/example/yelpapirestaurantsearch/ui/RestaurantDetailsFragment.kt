package com.example.yelpapirestaurantsearch.ui

import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.example.yelpapirestaurantsearch.R
import com.example.yelpapirestaurantsearch.viewModels.RestaurantsViewModel

/**
 * The [RestaurantDetailsFragment] displays the location of the restaurant on a map and
 * and some information below.
 */

class RestaurantDetailsFragment : RestaurantBaseFragment() {

    companion object {
        fun newInstance() = RestaurantDetailsFragment()
    }

    private lateinit var restaurantViewModel: RestaurantsViewModel
    private lateinit var mapView: MapView
    private lateinit var fragmentView: View
    val args: RestaurantDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            restaurantViewModel = ViewModelProviders.of(it).get(RestaurantsViewModel::class.java)
        }
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentView =  inflater.inflate(
                R.layout.restaurant_details_fragment, container, false)

        mapView = fragmentView.findViewById(R.id.restaurant_map_view)

        return  fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentView
                .findViewById<TextView>(R.id.restaurant_details_address_text_view)
                .apply {
                    transitionName = args.address
                    text = args.address
                }

    }

    override fun onResume() {
        super.onResume()

        restaurantViewModel.selectedBusiness.observe(this, Observer {


            // Load Map and its values.
            if (it.coordinates?.latitude != null && it.coordinates.longitude != null) {
                val location = Location("RestaurantBusinessLocation")
                location.latitude = it.coordinates.latitude
                location.longitude = it.coordinates.longitude
                mapView.addMarker(location, it.name)
            }


            //Load info below map
            restaurantViewModel.titleBarTitle.value = it.name
//            fragmentView
//                .findViewById<TextView>(R.id.restaurant_details_address_text_view)
//                .text = it.location?.address1
            fragmentView
                .findViewById<TextView>(R.id.restaurant_details_cost_indicator_view)
                .text = it.price
            fragmentView
                .findViewById<TextView>(R.id.restaurant_details_phone_view)
                .text = it.display_phone
        })

    }
}