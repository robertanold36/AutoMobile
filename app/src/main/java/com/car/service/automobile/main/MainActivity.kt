package com.car.service.automobile.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.car.service.automobile.R
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    var finalResult: String? = null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var addresses = arrayListOf<Address>()
    private val TAG = "MainActivity"
    lateinit var geocoder: Geocoder
    lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private val REQUEST_UPDATE_LOCATIONS_KEY = "tracking_location"
    private var mTrackingLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geocoder = Geocoder(this, Locale.getDefault())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            interval = 10000L
            fastestInterval = 5000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(REQUEST_UPDATE_LOCATIONS_KEY)
        }

        startLocationUpdate()
        btn_location.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                startLocationUpdate()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (mTrackingLocation)
                    startLocationUpdate()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBoolean(REQUEST_UPDATE_LOCATIONS_KEY, mTrackingLocation)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        if (mTrackingLocation) startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        if (mTrackingLocation) stopLocationUpdate()
        mTrackingLocation = false
    }

    private fun requestMyLocation(): LocationCallback {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                if (p0 != null) {
                    for (location in p0.locations) {

                        CoroutineScope(Dispatchers.Main).launch {

                            withContext(Dispatchers.IO) {
                                try {
                                    addresses = geocoder.getFromLocation(
                                        location.latitude,
                                        location.longitude,
                                        1
                                    )
                                            as ArrayList<Address>

                                    val address: Address = addresses[0]
                                    val addressPart = arrayListOf<String>()

                                    for (i in 0..address.maxAddressLineIndex) {
                                        Log.d(TAG, address.getAddressLine(0))
                                        addressPart.add(address.getAddressLine(i))
                                    }

                                    finalResult = addressPart[0]

                                } catch (io: IOException) {
                                    finalResult = "service not available at this time"
                                    Log.e(TAG, "service not available this time", io)

                                } catch (e: IllegalArgumentException) {
                                    finalResult = "invalid coordinate"
                                    Log.e(TAG, "invalid coordinate that have been supplied", e)
                                }

                            }

                            tv_location.text = location.latitude.toString()
                            tv_longitude.text = location.longitude.toString()
                            tv_address.text = finalResult
                        }

                    }
                    super.onLocationResult(p0)
                }
            }
        }

        return locationCallback
    }

    private fun startLocationUpdate() {
        mTrackingLocation = true
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            requestMyLocation(),
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdate() {
        mTrackingLocation = false
        fusedLocationProviderClient.removeLocationUpdates(requestMyLocation())
    }

}
