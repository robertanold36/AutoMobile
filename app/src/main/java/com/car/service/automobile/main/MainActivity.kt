package com.car.service.automobile.main

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.R
import com.car.service.automobile.repository.FirebaseInstance
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var map: GoogleMap
    private lateinit var mainViewModel: MainViewModel
    private val TAG = "MainActivity"
    private val permissionRequestCode = 1
    private val repository: FirebaseInstance by lazy {
        FirebaseInstance()
    }
    private var gps_enabled = false
    private var network_enabled = false
    private val runningQorLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Toast.makeText(this, "we can't access your location", Toast.LENGTH_LONG).show()
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Toast.makeText(this, "we can't access your location", Toast.LENGTH_LONG).show()
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        mainViewModel.getLocationData().observe(this, Observer {
            val zoomLevel = 16f
            val latLong = LatLng(it.latitude, it.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, zoomLevel))

        })

        btn_problem.setOnClickListener {
            if (isPermissionGranted()) {
                mainViewModel.getLocationData().observe(this, Observer {

                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            progressBar2.visibility = View.VISIBLE
                        }

                        val response = repository.getNearbyGarage(it.latitude, it.longitude)
                        when {
                            response.isSuccessful -> {

                                response.body().let {
                                    if (it != null) {
                                        withContext(Dispatchers.Main) {
                                            garageName.text = it[0].name
                                            progressBar2.visibility = View.GONE
                                            mainViewModel.getLocationData()
                                                .removeObservers(this@MainActivity)
                                        }

                                    }
                                }
                            }
                            response.code() == 400 -> {
                                withContext(Dispatchers.Main) {
                                    progressBar2.visibility = View.GONE
                                    garageName.text = getString(R.string.specifyLocation)
                                }
                            }
                            response.code() == 404 -> {
                                withContext(Dispatchers.Main) {
                                    progressBar2.visibility = View.GONE
                                    garageName.text = getString(R.string.serverError)
                                }
                            }
                            response.code() == 406 -> {
                                withContext(Dispatchers.Main) {
                                    progressBar2.visibility = View.GONE
                                    garageName.text = getString(R.string.fail)
                                }
                            }
                        }
                    }

                })

            } else {
                requestLocationPermission()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        if (isPermissionGranted()) {
            checkDeviceLocationSetting()
        } else {
            requestLocationPermission()
        }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            Snackbar.make(activity_main, "Please allow the location", Snackbar.LENGTH_LONG).show()
        }
    }


    @TargetApi(29)
    private fun isPermissionGranted(): Boolean {

        val foregroundLocationPermission =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))


        val backgroundLocationPermission = if (runningQorLater) {
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ))
        } else {
            true
        }

        return foregroundLocationPermission && backgroundLocationPermission

    }

    @TargetApi(29)
    private fun requestLocationPermission() {
        if (isPermissionGranted())
            return
        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQorLater -> {
                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE
            }
            else -> {
                REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE
            }
        }

        ActivityCompat.requestPermissions(this@MainActivity, permissionArray, resultCode)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionRequestCode) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }

        if (grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX]
                    == PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                activity_main,
                "please? allow location permission",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Allow") {
//                startActivity(Intent().apply {
//                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null
//                    )
//                })
                requestLocationPermission()
            }.show()
        } else {
            if (gps_enabled && network_enabled) {
                map.isMyLocationEnabled = true

            } else {
                checkDeviceLocationSetting()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSetting(false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSetting(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsRequest = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask = settingsRequest.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location setting resolution ${sendEx.message}")
                }
            } else {
                Snackbar.make(
                    activity_main,
                    "Please Turn on your location",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("ok") {
                        checkDeviceLocationSetting()
                    }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                if (isPermissionGranted()) {
                    map.isMyLocationEnabled = true
                } else {
                    requestLocationPermission()
                }

            }
        }

    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 33
