package com.car.service.automobile.main.ui

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.car.service.automobile.R
import com.car.service.automobile.databinding.ActivityHomeBinding
import com.car.service.automobile.utility.NetworkUtility.Companion.isPermissionGranted
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_information.*

class HomeActivity : AppCompatActivity() {

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val TAG = "HomeActivity"
    private var gpsEnabled = false
    private var networkEnabled = false
    private lateinit var binding: ActivityHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        val popupMenu = PopupMenu(this, iconMenu)
        popupMenu.menuInflater.inflate(R.menu.home_menu, popupMenu.menu)

        bottomSheetBehavior = BottomSheetBehavior.from(fragment_information)
        bottomSheetBehavior.peekHeight = 0


        iconMenu.setOnClickListener {
            popupMenu.setOnMenuItemClickListener { item ->
                when (item!!.itemId) {
                    R.id.contact -> {
                        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        } else {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                        }
                    }
                }
                true
            }
            popupMenu.show()

        }

        btn_problem.setOnClickListener {
            if (isPermissionGranted(this)||isGpsOrNetworkEnabled()) {
                val intent = Intent(this@HomeActivity, MainActivity::class.java)
                startActivity(intent)
            }else{
                checkPermission()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                checkLocationSettings()
            } else {
                Snackbar.make(
                    homeLayout,
                    "Please allow location permission",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Allow") {
                    requestLocationPermission()
                }.show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                checkLocationSettings(false)
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.e(TAG, "location setting enabled")
                    }
                    Activity.RESULT_CANCELED -> {
                        Snackbar.make(
                            homeLayout,
                            "Please Turn On Device Location",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Ok") {
                            checkLocationSettings()
                        }.show()
                    }
                }
            }
        }

    }

    @TargetApi(29)
    fun requestLocationPermission() {
        if (isPermissionGranted(this))
            return

        val permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(
            this,
            permissionArray,
            REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    fun checkLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setNeedBle(true)
        val task: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())



        task.addOnFailureListener { exception ->

            if (exception is ResolvableApiException && resolve) {
                try {

                    exception.startResolutionForResult(
                        this,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (e: ApiException) {
                    Log.d(TAG, "System fail to access your location")
                }
            } else {
                Log.e(TAG, "result cancelled")
                Toast.makeText(this, "result canceled", Toast.LENGTH_SHORT).show()

                Snackbar.make(
                    homeLayout,
                    "Please Turn On Device location",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkLocationSettings()
                }.show()
            }
        }

        task.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e(TAG, "Location setting are enabled to true")
            }
        }

    }

    private fun isGpsOrNetworkEnabled(): Boolean {

        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Log.e(TAG,"Fail to access gps location $e")
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        } catch (e: Exception) {
            Log.e(TAG,"Fail to access network location $e")
        }
        return gpsEnabled && networkEnabled
    }

    private fun checkPermission() {
        if (isPermissionGranted(this)) {
            checkLocationSettings()
        } else {
            requestLocationPermission()
        }
    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
