package com.example.yelpapirestaurantsearch

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.yelpapirestaurantsearch.ui.RestaurantListFragment
import com.example.yelpapirestaurantsearch.viewModels.RestaurantsViewModel

class MainActivity : AppCompatActivity() {

    lateinit var restaurantViewModel: RestaurantsViewModel
    var appBarConfiguration: AppBarConfiguration? = null
    var sharedPreference : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        restaurantViewModel = ViewModelProviders.of(this)
            .get(RestaurantsViewModel::class.java)

        restaurantViewModel.titleBarTitle.observe(this, Observer {
            supportActionBar?.title = it
        })

        sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        // This line is only necessary if using the default action bar.
        setupActionBarWithNavController(navController, appBarConfiguration!!)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle the back button event and return true to override
        // the default behavior the same way as the OnBackPressedCallback.
        // TODO(reason: handle custom back behavior here if desired.)

        // If no custom behavior was handled perform the default action.
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration!!) || super.onSupportNavigateUp()
    }


    override fun finish() {
        super.finish()

    }

    override fun onPause() {
        super.onPause()
        var editor = sharedPreference?.edit()
        editor?.putBoolean(RestaurantListFragment.REQUEST_LOCATION_FIRST_TIME, true)
        editor?.commit()
    }


}