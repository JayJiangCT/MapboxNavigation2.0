package com.wonder.mapbox.mapboxnavigationv2demo.ui

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.toCameraOptions
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.wonder.mapbox.mapboxnavigationv2demo.R
import com.wonder.mapbox.mapboxnavigationv2demo.databinding.ActivityRoutePreviewBinding
import com.wonder.mapbox.mapboxnavigationv2demo.ui.base.BaseMapViewActivity

/**
 * author jiangjay on  27-07-2021
 */
class RoutePreviewActivity : BaseMapViewActivity<ActivityRoutePreviewBinding>(), OnMapLongClickListener {

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        Log.d("TAG", "[${it.latitude()}, ${it.longitude()}]")
        startPoint = it
        // Jump to the current indicator position
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        // Set the gestures plugin's focal point to the current indicator location.
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
     */
    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build()
            )
        }
    }

    private var startPoint: Point? = null

    private var endPoint: Point? = null

    override val mapView: MapView
        get() = binding.mapView

    override fun mapReady() {
        mapView.location.apply {
            enabled = true
            pulsingEnabled = true
            locationPuck = LocationPuck2D(
                topImage = AppCompatResources.getDrawable(
                    this@RoutePreviewActivity,
                    R.drawable.ic_puck
                )
            )
        }
        mapView.getMapboxMap().apply {
            addOnMapLongClickListener(this@RoutePreviewActivity)
        }
    }

    override fun inflateBinding(): ActivityRoutePreviewBinding = ActivityRoutePreviewBinding.inflate(layoutInflater)

    override fun onStart() {
        super.onStart()
        mapView.location.addOnIndicatorPositionChangedListener()
    }

    override fun onStop() {
        super.onStop()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onMapLongClick(point: Point): Boolean {
        endPoint = point
        return true
    }

    private fun fetchRoute(point: Point) {
        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            .coordinatesList(listOf(startPoint, endPoint))
            .alternatives(false)
            .build()
        mapboxNavigation.requestRoutes(routeOptions, object : RouterCallback {
            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                TODO("Not yet implemented")
            }

            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                TODO("Not yet implemented")
            }

            override fun onRoutesReady(routes: List<DirectionsRoute>, routerOrigin: RouterOrigin) {
                TODO("Not yet implemented")
            }
        })
    }
}
