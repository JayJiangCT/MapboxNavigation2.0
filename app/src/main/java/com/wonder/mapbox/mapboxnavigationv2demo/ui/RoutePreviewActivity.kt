package com.wonder.mapbox.mapboxnavigationv2demo.ui

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
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
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.wonder.mapbox.mapboxnavigationv2demo.R
import com.wonder.mapbox.mapboxnavigationv2demo.databinding.ActivityRoutePreviewBinding
import com.wonder.mapbox.mapboxnavigationv2demo.ui.base.BaseMapViewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * author jiangjay on  27-07-2021
 */
class RoutePreviewActivity : BaseMapViewActivity<ActivityRoutePreviewBinding>(), OnMapLongClickListener,
    CoroutineScope by MainScope() {

    companion object {

        private const val ICON_DESTINATION = "icon-destination"
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        startPoint = it
        if (cameraOptions == null) {
            // Jump to the current indicator position
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build().also { options ->
                cameraOptions = options
            })
            // Set the gestures plugin's focal point to the current indicator location.
            mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        }
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

    private var cameraOptions: CameraOptions? = null

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
        launch {
            val style = mapView.getMapboxMap().getStyle()
            style?.removeStyleSource("mapbox-navigation-waypoint-source")
            convertDrawableToBitmap(R.drawable.ic_location)?.let {
                mapView.getMapboxMap().getStyle()?.addImage(ICON_DESTINATION, it)
            }
        }
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
        addEndPointMarker(point)
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

    private fun addMarker(point: Point, @DrawableRes drawableId: Int) {
        val annotationsApi = mapView.annotations
        val pointAnnotationManager = annotationsApi.createPointAnnotationManager(mapView)
        launch {
            convertDrawableToBitmap(drawableId)?.let {
                val pointAnnotationOptions = PointAnnotationOptions().withPoint(point).withIconImage(it)
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }
    }

    private fun addEndPointMarker(point: Point) {
        val annotationsApi = mapView.annotations
        val pointAnnotationManager = annotationsApi.createPointAnnotationManager(mapView)
        val pointAnnotationOptions =
            PointAnnotationOptions().withPoint(point).withIconImage(ICON_DESTINATION).withIconAnchor(IconAnchor.TOP)
        pointAnnotationManager.create(pointAnnotationOptions)
    }

    private suspend fun convertDrawableToBitmap(@DrawableRes drawableId: Int) = withContext(Dispatchers.IO) {
        AppCompatResources.getDrawable(this@RoutePreviewActivity, drawableId)?.toBitmap()
    }
}
