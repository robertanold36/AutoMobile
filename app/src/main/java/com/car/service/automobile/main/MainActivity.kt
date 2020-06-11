package com.car.service.automobile.main

import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.car.service.automobile.R
import com.car.service.automobile.repository.FirebaseInstance
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private val REQUEST_UPDATE_LOCATION_KEY = "tracking_location"
    private var mTrackingLocation = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    private val TAG = "MainActivity"

    private val repository: FirebaseInstance by lazy {
        FirebaseInstance()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            interval = 10000L
            fastestInterval = 5000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(REQUEST_UPDATE_LOCATION_KEY)
        }

        startLocationUpdate()

        if (mTrackingLocation) {
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync(this)

        }

        cardDirectly.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
        }

        btn_problem.setOnClickListener {

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                requestService(location.latitude, location.longitude)
            }


        }

        cardSpare.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBoolean(REQUEST_UPDATE_LOCATION_KEY, mTrackingLocation)
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

    private fun startLocationUpdate() {
        mTrackingLocation = true
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, requestMyLocation(), Looper.getMainLooper()
        )
    }

    private fun requestMyLocation(): LocationCallback {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                if (p0 != null) {
                    for (location in p0.locations) {

                        val latLng = LatLng(location.latitude, location.longitude)
                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        map.moveCamera(update)
                    }
                }
                super.onLocationResult(p0)
            }
        }
        return locationCallback
    }

    private fun stopLocationUpdate() {
        mTrackingLocation = false
        fusedLocationProviderClient.removeLocationUpdates(requestMyLocation())
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.isMyLocationEnabled = true
    }


    private fun requestService(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                progressBar2.visibility = View.VISIBLE
            }

            val response = repository.getNearbyGarage(latitude, longitude)
            when {
                response.isSuccessful -> {

                    response.body().let {
                        if (it != null) {
                            withContext(Dispatchers.Main) {
                                garageName.text = it[0].name
                                progressBar2.visibility = View.GONE
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
    }

}
