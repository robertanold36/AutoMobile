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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var locationCallback: LocationCallback
    private var locationRequest:LocationRequest?=null
    private val REQUEST_UPDATE_LOCATION_KEY="tracking_location"
    private var mTrackingLocation=false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
     var latitude:Double?=null
     var longitude: Double?=null

    private val TAG = "MainActivity"

    private val repository: FirebaseInstance by lazy {
        FirebaseInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        locationRequest=LocationRequest().apply {
            interval=10000L
            fastestInterval=5000L
            priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if(savedInstanceState!=null){
            mTrackingLocation=savedInstanceState.getBoolean(REQUEST_UPDATE_LOCATION_KEY)
        }

        startLocationUpdate()

        cardDirectly.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
        }

        btn_problem.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                progressBar2.visibility=View.VISIBLE
                val response = repository.getNearbyGarage(latitude!!,longitude!!)
                when {
                    response.isSuccessful -> {

                        response.body().let {
                            if (it != null) {
                                garageName.text=it[0].name
                                progressBar2.visibility=View.GONE
                            }
                        }
                    }
                    response.code()==400 -> {
                        progressBar2.visibility=View.GONE
                        garageName.text= getString(R.string.specifyLocation)
                    }
                    response.code()==404 -> {
                        progressBar2.visibility=View.GONE
                        garageName.text=getString(R.string.serverError)
                    }
                    response.code()==406 -> {
                        progressBar2.visibility=View.GONE
                        garageName.text=getString(R.string.fail)
                    }
                }
            }

        }

            cardSpare.setOnClickListener {
                Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()

            }
        }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBoolean(REQUEST_UPDATE_LOCATION_KEY,mTrackingLocation)
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
        mTrackingLocation=true
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,requestMyLocation(), Looper.getMainLooper()
        )
    }

    private fun requestMyLocation():LocationCallback{
        locationCallback= object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                if(p0!=null){
                    for(location in p0.locations){
                        latitude=location.latitude
                        longitude=location.longitude
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
}
