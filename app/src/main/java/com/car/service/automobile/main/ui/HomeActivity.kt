package com.car.service.automobile.main.ui

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.car.service.automobile.BuildConfig
import com.car.service.automobile.R
import com.car.service.automobile.Resource
import com.car.service.automobile.model.garage
import com.car.service.automobile.main.MainActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var map: GoogleMap
    private val permissionRequestCode = 1
    private lateinit var fStore: FirebaseFirestore
    lateinit var adapter: ArrayAdapter<String>
    private val TAG = "HomeActivity"
    private val runningQorLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fStore = FirebaseFirestore.getInstance()
        val carList = resources.getStringArray(R.array.carModel)
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, carList)
        search_bar.queryHint = "car model!! Toyota,Nissan"

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        search_bar.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    if (query.isNotEmpty()) {
                        adapter.filter.filter(query).also {
                            listView.adapter = adapter
                        }

                    } else {
                        listView.adapter = null
                    }
                } else {
                    adapter.clear()
                }


                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isNotEmpty()) {
                        adapter.filter.filter(newText).let {
                            listView.adapter = adapter

                        }
                    } else {
                        listView.adapter = null
                    }
                } else {
                    adapter.clear()
                }

                return false
            }
        })

        listView.setOnItemClickListener { parent, view, position, id ->
            val name = parent.getItemAtPosition(position) as String
            val intent = Intent(this@HomeActivity, MainActivity::class.java)
            intent.putExtra("car_model", name)
            if (isPermissionGranted()) {
                startActivity(intent)
            } else {
                requestLocationPermission()
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        val latitude = -6.8137612
        val longitude = 39.1824866

        fStore.collection("Garage")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {

                } else {
                    if (querySnapshot != null) {
                        for (document in querySnapshot.documents) {
                            val latitude = document.get("latitude").toString().toDouble()
                            val longitude = document.get("longitude").toString().toDouble()
                            val hmlat = LatLng(latitude, longitude)
                            map.addMarker(
                                MarkerOptions().position(hmlat)
                                    .icon(bitmapMarker(this, R.drawable.ic_car_repair))
                            )
                        }

                    }
                }
            }

        val zoomLevel = 13f
        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        //setStyle(map)
        enableMyLocation()

    }

    override fun onStart() {
        super.onStart()
        checkDeviceLocationSettingAndCreateGeofence()
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


    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
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

        ActivityCompat.requestPermissions(this@HomeActivity, permissionArray, resultCode)
    }

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
                activity_home,
                "please? make sure you allow the location permission",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("go to setting") {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                })
            }.show()
        } else {
            checkDeviceLocationSettingAndCreateGeofence()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_TURN_DEVICE_ON){
            checkDeviceLocationSettingAndCreateGeofence(false)
        }
    }

    private fun checkDeviceLocationSettingAndCreateGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsRequest=LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask=settingsRequest.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener {exception ->
            if(exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this@HomeActivity, REQUEST_TURN_DEVICE_ON)
                }catch (sendEx:IntentSender.SendIntentException){
                    Log.d(TAG,"Error getting location setting resolution ${sendEx.message}")
                }
            }else{
                Snackbar.make(activity_home,"turn on your location",Snackbar.LENGTH_INDEFINITE).setAction("ok"){
                    checkDeviceLocationSettingAndCreateGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if(it.isSuccessful){
                enableMyLocation()
            }
        }

    }

    private fun bitmapMarker(context: Context, vectorIcon: Int): BitmapDescriptor? {
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
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 33


