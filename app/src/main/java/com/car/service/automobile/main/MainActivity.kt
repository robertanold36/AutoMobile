package com.car.service.automobile.main

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.R
import com.car.service.automobile.Resource
import com.car.service.automobile.repository.ApiRepository
import com.car.service.automobile.utility.NetworkUtility.Companion.isPermissionGranted
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mainViewModel: MainViewModel
    private val TAG = "MainActivity"
    private lateinit var map: GoogleMap
    private var checkedItem = 1
    lateinit var binding: com.car.service.automobile.databinding.ActivityMainBinding
    lateinit var mapFragment: SupportMapFragment
    private val runningQorLater =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)

        val apiRepository = ApiRepository()
        val mainViewModelFactory = MainViewModelFactory(apiRepository, application)
        mainViewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)
        binding.mainViewModel = mainViewModel

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(fragment_request)
        bottomSheetBehavior.peekHeight = 0

        val carModel = resources.getStringArray(R.array.carModel)

        btn_problem.setOnClickListener {
            if (isPermissionGranted(this)) {
                if (mainViewModel.isGpsOrNetworkEnabled()) {

                    requestWorkshop()

                } else {
                    checkLocationSettings()
                }
            } else {
                requestLocationPermission()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isPermissionGranted(this)) {
            checkLocationSettings()
        } else {
            requestLocationPermission()
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
                            binding.activityMain,
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

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        enableMyLocation()
        moveCamera()
        setMapStyle(map)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX]
                    == PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                activity_main,
                "Please allow location permission",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Allow") {
                requestLocationPermission()
            }.show()

        } else {
            checkLocationSettings()
        }
    }


    @TargetApi(29)
    fun requestLocationPermission() {
        if (isPermissionGranted(this))
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

        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissionArray,
            resultCode
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
                    binding.activityMain,
                    "Please Turn On Device Location To find Your pickup location",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkLocationSettings()
                }.show()
            }
        }

        task.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e(TAG, "Location setting are enabled to true")
                getNearbyGarage()
            }
        }

    }

    private fun createMarker(context: Context, vectorIcon: Int): BitmapDescriptor {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorIcon)
        vectorDrawable?.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.minimumWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            if (success) {
                Log.d(TAG, "Map has been styled")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d(TAG, "resources not found")
        }
    }

    private fun getNearbyGarage() {
        mainViewModel.garageList.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { garageList ->
                        if (garageList != null) {
                            val results = garageList.result
                            for (result in results) {
                                val lat = result.location.coordinates[0]
                                val lon = result.location.coordinates[1]
                                map.addMarker(
                                    MarkerOptions().icon(
                                        createMarker(
                                            this,
                                            R.drawable.ic_car_repair
                                        )
                                    ).position(LatLng(lat, lon)).visible(true)
                                )

                            }
                            mainViewModel.garageList.removeObservers(this)
                        }
                    }
                }
                is Resource.Error -> {
                    response.message.let {
                        if (it != null) {
                            Snackbar.make(binding.activityMain, it, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Try again") {
                                    getNearbyGarage()
                                }
                                .show()
                            mainViewModel.garageList.removeObservers(this)
                        }
                    }

                }
            }
        })

    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted(this)) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun moveCamera() {

        mainViewModel.getLocationData().observe(this, Observer {

            CoroutineScope(Dispatchers.IO).launch {
                mainViewModel.getAllNearByGarage(it.latitude, it.longitude)

            }
            val latLong = LatLng(it.latitude, it.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 12f))
        })
    }

    private fun requestWorkshop() {
        mainViewModel.garageList.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data.let {
                        if (it != null) {

                            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                                val workshopData = it.result[0]
                                CoroutineScope(Dispatchers.IO).launch {
                                    mainViewModel.requestWorkshop(workshopData.workshopID)

                                    withContext(Dispatchers.Main) {
                                        mainViewModel.workshop.observe(
                                            this@MainActivity,
                                            Observer { response ->
                                                when (response) {
                                                    is Resource.Loading -> {
                                                        pb.visibility = View.VISIBLE
                                                    }

                                                    is Resource.Success -> {
                                                        pb.visibility = View.INVISIBLE
                                                        response.data.let { result ->
                                                            if (result != null) {
                                                                workshopName.text = result.name
                                                                workshopRate.text =
                                                                    result.CompanyReview.toString()
                                                                phoneNumber.text = result.contacts
                                                                mainViewModel.workshop.removeObservers(
                                                                    this@MainActivity
                                                                )
                                                            } else {
                                                                Snackbar.make(
                                                                    fragment_request,
                                                                    "Hakuna Garage ya karibu",
                                                                    Snackbar.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                                    is Resource.Error -> {
                                                        pb.visibility = View.INVISIBLE
                                                        resource.message.let { error ->
                                                            Snackbar.make(
                                                                fragment_request,
                                                                "$error",
                                                                Snackbar.LENGTH_LONG
                                                            ).show()

                                                        }
                                                    }
                                                }
                                            })
                                    }
                                }


                            } else {
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            }

                        } else {
                            Snackbar.make(
                                binding.activityMain,
                                "There is no garage at your location",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                    mainViewModel.garageList.removeObservers(this)
                }
                is Resource.Error -> {
                    resource.message.let {
                        if (it != null) {
                            Snackbar.make(binding.activityMain, it, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Try again") {
                                    getNearbyGarage()
                                }
                                .show()
                            mainViewModel.garageList.removeObservers(this)
                        }
                    }

                }
            }
        })
    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
