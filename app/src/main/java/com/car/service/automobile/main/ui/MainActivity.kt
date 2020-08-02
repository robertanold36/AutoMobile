package com.car.service.automobile.main.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import com.car.service.automobile.utility.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
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


        btn_request.setOnClickListener {
            observeWorkshops()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        enableMyLocation()
        moveCamera()
        setMapStyle(map)
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
                is Resource.Loading->{
                    progressBar2.visibility=View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar2.visibility=View.INVISIBLE
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
                    progressBar2.visibility=View.INVISIBLE
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
        map.isMyLocationEnabled = true
        getNearbyGarage()

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

            val alertDialog = AlertDialog.Builder(this)
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

