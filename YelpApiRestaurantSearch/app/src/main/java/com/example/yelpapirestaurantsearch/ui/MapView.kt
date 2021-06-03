package com.example.yelpapirestaurantsearch.ui

import android.content.Context
import android.location.Location
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.yelpapirestaurantsearch.SearchRestaurantsQuery
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

/**
 * This []MapView] enables us to use it any ViewGroup. it also abstracts the implementation of the
 * map and makes it easy to use.
 *
 */

class MapView: FrameLayout {

    private var mapSubject: Subject<GoogleMap>? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    {
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    {
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)
    {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val mapFragment = SupportMapFragment.newInstance()
        if (!isInEditMode) {
            val fragmentTransaction: FragmentTransaction =
                (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.add(id, mapFragment)
            fragmentTransaction.commit()


            mapSubject = BehaviorSubject.create()
            Observable.create(
                    ObservableOnSubscribe<GoogleMap> {
                        e -> mapFragment.getMapAsync(e::onNext) }
                            as ObservableOnSubscribe<GoogleMap?>?)
                    .subscribe(mapSubject)
        }
    }

    fun addMarker(location: Location, name: String? = "Restaurant") {

        mapSubject?.subscribe { googleMap ->

            val position = LatLng(location.latitude, location.longitude)

            val cameraPosition : CameraPosition = CameraPosition.Builder()
                    .target(position)
                    .zoom(17.0f)
                    .build()

            googleMap.addMarker(MarkerOptions().position(position).title(name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(position))

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }
}