package com.car.service.automobile.main

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.car.service.automobile.model.LatLongitude
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

 class LocationTracking(context: Context): LiveData<LatLongitude>() {

    private var fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(context)

    companion object{
        val locationRequest:LocationRequest= LocationRequest.create().apply {
            interval=1000
            fastestInterval=5000
            priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val locationCallBack= object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (p0 == null) return
            for(location in p0.locations){
                setLocation(location)
            }
        }
    }

     @SuppressLint("MissingPermission")
     private fun startLocationUpdate(){
         fusedLocationProviderClient.requestLocationUpdates(
             locationRequest,
             locationCallBack,
             null
         )
     }

    private fun setLocation(location:Location){
        value= LatLongitude(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        startLocationUpdate()
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }
}