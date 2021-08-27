package com.wonder.mapbox.mapboxnavigationv2demo.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.wonder.mapbox.mapboxnavigationv2demo.R
import com.wonder.mapbox.mapboxnavigationv2demo.databinding.ActivityRoutePreviewBinding
import com.wonder.mapbox.mapboxnavigationv2demo.ui.base.BaseMapViewActivity

/**
 * author jiangjay on  27-07-2021
 */
class RoutePreviewActivity : BaseMapViewActivity<ActivityRoutePreviewBinding>() {

//    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
//        // Jump to the current indicator position
//        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
//        // Set the gestures plugin's focal point to the current indicator location.
//        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
//    }

    override val mapView: MapView
        get() = binding.mapView

    override fun mapReady() {
        mapView.location.apply {
            enabled = true
            locationPuck = LocationPuck2D(
                topImage = AppCompatResources.getDrawable(
                    this@RoutePreviewActivity,
                    R.drawable.ic_puck
                )
            )
        }
        mapView.getMapboxMap().apply {
        }
    }

    override fun inflateBinding(): ActivityRoutePreviewBinding = ActivityRoutePreviewBinding.inflate(layoutInflater)
