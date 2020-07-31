package com.car.service.automobile.main.ui

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.R
import com.car.service.automobile.main.MainViewModel
import com.car.service.automobile.main.MainViewModelFactory
import com.car.service.automobile.model.GarageResult
import com.car.service.automobile.model.NotificationData
import com.car.service.automobile.model.PushNotification
import com.car.service.automobile.model.WorkShopResponseX
import com.car.service.automobile.repository.ApiRepository
import com.car.service.automobile.utility.Constants.Companion.COLLECTION
import com.car.service.automobile.utility.Constants.Companion.EMAIL
import com.car.service.automobile.utility.Constants.Companion.TOKEN
import com.car.service.automobile.utility.Listener
import com.car.service.automobile.utility.NetworkUtility.Companion.isPermissionGranted
import com.car.service.automobile.utility.Resource
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    lateinit var mainViewModel: MainViewModel
    private val TAG = "MainActivity"
    private lateinit var map: GoogleMap
    private var checkedItem = 1
    lateinit var binding: com.car.service.automobile.databinding.ActivityMainBinding
    lateinit var mapFragment: SupportMapFragment
    private val runningQorLater =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var coordinatorLayout: CoordinatorLayout

    private val fStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val fAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(toolbar)
        coordinatorLayout = binding.activityMain
        val apiRepository = ApiRepository()
        val mainViewModelFactory =
            MainViewModelFactory(
                apiRepository,
                application
            )
        mainViewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)
        binding.mainViewModel = mainViewModel
        mainViewModel.listener = this

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(fragment_request)
        bottomSheetBehavior.peekHeight = 0


        btn_problem.setOnClickListener {
            if (isPermissionGranted(this)) {
                if (mainViewModel.isGpsOrNetworkEnabled()) {

                    observeWorkshops()

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
                            coordinatorLayout,
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
                    coordinatorLayout,
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
                            Snackbar.make(coordinatorLayout, it, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Try again") {
                                    moveCamera()
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

    private fun observeWorkshops() {

        mainViewModel.garageList.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data.let {
                        if (it != null) {
                            requestWorkshop(it)
                        } else {
                            Snackbar.make(
                                coordinatorLayout,
                                "There is no garage at your bound",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }

                    mainViewModel.garageList.removeObservers(this)
                }

                is Resource.Error -> {
                    resource.message.let {
                        if (it != null) {
                            Snackbar.make(coordinatorLayout, it, Snackbar.LENGTH_INDEFINITE)
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

    private fun sendNotification(email: String) {

        val uid = fAuth.currentUser?.uid
        if (uid != null) {
            Log.e(TAG, uid)
            mainViewModel.getUserDetail(uid)

            mainViewModel.userDetail.observe(this, Observer { user ->
                Log.e(TAG, user.toString())
                CoroutineScope(Dispatchers.IO).launch {
                    fStore.collection(COLLECTION).whereEqualTo(EMAIL, email).get()
                        .addOnSuccessListener {
                            for (document in it.documents) {
                                val token = document.get(TOKEN).toString()
                                Log.e(TAG, token)
                                val notificationData =
                                    NotificationData("Request Service", user.phoneNumber, user.name)
                                val pushNotification = PushNotification(notificationData, token)
                                mainViewModel.sendNotification(pushNotification)
                            }
                        }
                }
            })
        }

    }

    private fun requestWorkshop(garageResult: GarageResult) {


        try {
            var selected: String? = null
            val carModel = resources.getStringArray(R.array.carModel)

            val alertDialog = AlertDialog.Builder(applicationContext)
            alertDialog.setTitle("Car Model")
            alertDialog.setCancelable(false)

            alertDialog.setPositiveButton("Yes") { _, item ->
                when (item) {
                    item -> {
                        for (workshopData in garageResult.result) {
                            if (workshopData.VehicleBrands.contains(selected)) {

                                CoroutineScope(Dispatchers.IO).launch {
                                    Log.e(TAG, workshopData.toString())
                                    mainViewModel.requestWorkshop(workshopData.workshopID)

                                    withContext(Dispatchers.Main) {

                                        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                                            bottomSheetBehavior.state =
                                                BottomSheetBehavior.STATE_EXPANDED

                                        } else {

                                            bottomSheetBehavior.state =
                                                BottomSheetBehavior.STATE_COLLAPSED
                                        }

                                    }

                                }
                                break
                            } else {
                                Snackbar.make(
                                    coordinatorLayout,
                                    "Cant fix your car type",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

            }

            alertDialog.setNeutralButton("Cancel") { _, _ ->
                Toast.makeText(this@MainActivity, "You Cancel", Toast.LENGTH_LONG)
                    .show()
            }

            alertDialog.setSingleChoiceItems(carModel, checkedItem) { _, item ->
                when (item) {
                    item -> {
                        selected = carModel[item]

                    }
                }
            }
            val mAlertDialog = alertDialog.create()
            mAlertDialog.show()

        } catch (e: NullPointerException) {
            Snackbar.make(
                coordinatorLayout,
                "There is no garage at your location bro",
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

    override fun onLoading() {
        pb.visibility = View.VISIBLE
    }

    override fun onError(message: String) {
        pb.visibility = View.INVISIBLE

        Snackbar.make(
            activity_main,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onSuccess(data: WorkShopResponseX) {

        pb.visibility = View.INVISIBLE

        sendNotification(data.email)

        workshopName.text =
            data.name

        workshopRate.apply {
            numStars = data.CompanyReview
            isIndicator

        }

        phoneNumber.text =
            data.contacts

    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
