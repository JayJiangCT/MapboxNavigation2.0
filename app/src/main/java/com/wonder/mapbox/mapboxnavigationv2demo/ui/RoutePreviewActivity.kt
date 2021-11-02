package com.wonder.mapbox.mapboxnavigationv2demo.ui

import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
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

    private val routeOptions by lazy {
        MapboxRouteLineOptions.Builder(this@RoutePreviewActivity)
            .withRouteLineResources(
                RouteLineResources.Builder()
                    .routeLineColorResources(RouteLineColorResources.Builder().build())
                    .build()
            ).build()
    }

    private val routeLineView by lazy {
        MapboxRouteLineView(routeOptions)
    }

    private val routeLineApi by lazy {
        MapboxRouteLineApi(routeOptions)
    }

    private var startPoint: Point? = null

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
        mapView.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onStop() {
        super.onStop()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onMapLongClick(point: Point): Boolean {
        fetchRoute(point)
        return true
    }

    private fun fetchRoute(point: Point) {
        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            .coordinatesList(listOf(startPoint, point))
            .alternatives(false)
            .build()
        mapboxNavigation.requestRoutes(routeOptions, object : RouterCallback {
            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
            }

            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
            }

            override fun onRoutesReady(routes: List<DirectionsRoute>, routerOrigin: RouterOrigin) {
                routeLineApi.setRoutes(routes.map { RouteLine(it, null) }) { expect ->
                    mapView.getMapboxMap().getStyle { style ->
                        routeLineView.renderRouteDrawData(style, expect)
                    }
                }
            }
        })
    }
}
