package com.wonder.mapbox.mapboxnavigationv2demo.ui

import android.graphics.Color
import android.os.Bundle
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.ROUND
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.AnnotationManager
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.extensions.coordinates
import com.mapbox.navigation.ui.maps.building.api.MapboxBuildingsApi
import com.mapbox.navigation.ui.maps.building.model.MapboxBuildingHighlightOptions
import com.mapbox.navigation.ui.maps.building.view.MapboxBuildingView
import com.wonder.mapbox.mapboxnavigationv2demo.R
import com.wonder.mapbox.mapboxnavigationv2demo.Utils
import com.wonder.mapbox.mapboxnavigationv2demo.databinding.ActivityHignlightBuildingBinding
import com.wonder.mapbox.mapboxnavigationv2demo.ui.base.BaseActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * author jiangjay on  16-11-2021
 */
class HighlightBuildingActivity : BaseActivity<ActivityHignlightBuildingBinding>() {

    private val annotationApi by lazy {
        binding.mapView.annotations
    }

    private var polylineAnnotationManager: PolylineAnnotationManager? = null

    override fun inflateBinding(): ActivityHignlightBuildingBinding =
        ActivityHignlightBuildingBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.mapView.getMapboxMap().loadStyleUri("mapbox://styles/mapbox-uat/cktjr0l3q4a1b18wbn23a9dhy")
        initLocationComponent()
    }

    override fun onDestroy() {
        super.onDestroy()
        annotationApi.cleanup()
    }

    private var currentPoint: Point? = null

    private val positionChangedListener = OnIndicatorPositionChangedListener { point ->
        if (currentPoint == null) {
            binding.mapView.camera.easeTo(
                CameraOptions.Builder().center(point).zoom(15.0).pitch(0.0).build(),
                mapAnimationOptions {
                    duration(1000L)
                })
        }
        currentPoint = point
    }

    private fun initLocationComponent() {
        binding.mapView.location.apply {
            enabled = true
            pulsingEnabled = true
            addOnIndicatorPositionChangedListener(positionChangedListener)
        }
        binding.mapView.gestures.addOnMapLongClickListener { point ->
            Utils.vibrate(this)
            fetchRoute(point)
            highlightBuilding(point)
            true
        }
    }

    private fun fetchRoute(point: Point) {
        MapboxDirections
            .builder()
            .routeOptions(
                RouteOptions.builder()
                    .applyDefaultNavigationOptions()
                    .applyLanguageAndVoiceUnitOptions(this)
                    .coordinates(origin = currentPoint!!, destination = point)
                    .alternatives(false)
                    .build()
            )
            .accessToken(getString(R.string.mapbox_access_token))
            .build()
            .enqueueCall(object : Callback<DirectionsResponse> {
                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    if (!response.body()?.routes().isNullOrEmpty()) {
                        val route = response.body()!!.routes().first()
                        drawRoutePath(route.geometry()!!)
                        val coordinates = mutableListOf<Point>()
                        coordinates.add(currentPoint!!)
                        coordinates.addAll(PolylineUtils.decode(route.geometry()!!, Constants.PRECISION_6))
                        boundCamera(coordinates)
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                }
            })
    }

    private fun drawRoutePath(geoString: String) {
        if (polylineAnnotationManager == null) {
            annotationApi.createPolylineAnnotationManager(binding.mapView).apply {
                lineCap = LineCap.ROUND
                lineDasharray = listOf(0.01, 2.0)
            }.also {
                polylineAnnotationManager = it
            }
        } else {
            polylineAnnotationManager?.deleteAll()
        }
        val polylineAnnotationOptions = PolylineAnnotationOptions()
            .withGeometry(
                LineString.fromPolyline(
                    geoString,
                    Constants.PRECISION_6
                )
            )
            .withLineJoin(ROUND)
            .withLineColor("#3975CC")
            .withLineWidth(10.0)
            .withLineOffset(10.0)
        polylineAnnotationManager?.create(polylineAnnotationOptions)
    }

    private fun highlightBuilding(point: Point) {
        val mapboxMap = binding.mapView.getMapboxMap()
        val buildingApi = MapboxBuildingsApi(mapboxMap)
        buildingApi.queryBuildingToHighlight(point) { expected ->
            expected.fold({}, { value ->
                val buildingView = MapboxBuildingView()
                val highlightOptions = MapboxBuildingHighlightOptions.Builder()
                    .fillExtrusionColor(Color.parseColor("#E1DAC8"))
                    .fillExtrusionOpacity(0.5)
                    .build()
                mapboxMap.getStyle() { style ->
                    buildingView.highlightBuilding(
                        style,
                        value.buildings,
                        highlightOptions
                    )
                }
            })
        }
    }

    private fun boundCamera(coordinates: List<Point>) {
        val mapboxMap = binding.mapView.getMapboxMap()
        mapboxMap.easeTo(
            mapboxMap.cameraForCoordinates(
                coordinates,
                EdgeInsets(50.0, 50.0, 50.0, 50.0)
            ), mapAnimationOptions {
                duration(1000L)
            })
    }
}