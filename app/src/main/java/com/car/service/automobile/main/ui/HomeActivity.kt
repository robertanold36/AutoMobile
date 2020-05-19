package com.car.service.automobile.main.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.car.service.automobile.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var map: GoogleMap
    private val permissionRequestCode = 1
    private lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var adapter: ArrayAdapter<String>
    private val TAG = "HomeActivity"
    lateinit var carList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firebaseFirestore = FirebaseFirestore.getInstance()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


        search_bar.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                carList = getCar(query!!)
                adapter =
                    ArrayAdapter<String>(
                        this@HomeActivity,
                        android.R.layout.simple_list_item_1,
                        carList
                    )
                listView.adapter = adapter

                return true

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                carList = getCar(newText!!)
                adapter =
                    ArrayAdapter<String>(
                        this@HomeActivity,
                        android.R.layout.simple_list_item_1,
                        carList
                    )
                listView.adapter = adapter
                return true
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        val latitude = -6.8137612
        val longitude = 39.1824866

        firebaseFirestore.collection("Garage")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {

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


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionRequestCode
            )
        }
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

    private fun getCar(search: String): MutableList<String> {
        val allCar: MutableList<String> = mutableListOf()
        firebaseFirestore.collection("cars").whereEqualTo("carName", search)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    //
                } else {
                    if (querySnapshot != null) {
                        for (document in querySnapshot.documents) {
                            val car = document.get("carName").toString()
                            Log.d(TAG, car)
                            allCar.add(car)
                        }

                    }
                }
            }
        return allCar
    }
}
