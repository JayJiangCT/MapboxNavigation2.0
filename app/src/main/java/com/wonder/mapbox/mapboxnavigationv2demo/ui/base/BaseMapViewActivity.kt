package com.wonder.mapbox.mapboxnavigationv2demo.ui.base

import android.Manifest
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.Style.Companion
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * author jiangjay on  27-07-2021
 */

private const val RC_LOCATION = 0x70
private val permissions =
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

abstract class BaseMapViewActivity<B : ViewBinding> : BaseActivity<B>(), EasyPermissions.PermissionCallbacks {

    protected abstract val mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermissions()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    @AfterPermissionGranted(RC_LOCATION)
    private fun checkLocationPermissions() {
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            requestPermissions()
        } else {
            mapInitialization()
        }
    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            "We need your location, storage read and write permissions, please open it",
            RC_LOCATION,
            *permissions
        )
    }

    private fun mapInitialization() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            mapReady()
        }
    }

    protected abstract fun mapReady()

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}