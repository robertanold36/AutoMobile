package com.car.service.automobile.main

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.car.service.automobile.SystemApplication
import com.car.service.automobile.utility.LocationEventListener
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    private val locationUpdate = LocationTracking(app)

    fun getLocationData() = locationUpdate


    private val TAG = "MainActivity"

    var gps_enabled = false
    var network_enabled = false


    lateinit var locationListener: LocationEventListener

    fun isGpsOrNetworkEnabled(): Boolean {

        val locationManager =
            getApplication<SystemApplication>().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            //
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        } catch (e: Exception) {
            //
        }

        return gps_enabled && network_enabled
    }




}