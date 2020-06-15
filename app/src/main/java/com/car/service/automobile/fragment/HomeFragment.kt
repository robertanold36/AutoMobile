package com.car.service.automobile.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.car.service.automobile.R
import com.car.service.automobile.databinding.FragmentHomeBinding
import com.car.service.automobile.main.MainActivity
import com.car.service.automobile.main.MainViewModel
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class HomeFragment : Fragment(), OnMapReadyCallback {
    private val runningQorLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    lateinit var mainViewModel: MainViewModel
    private val TAG = "MainActivity"
    private lateinit var map: GoogleMap
    private val permissionRequestCode = 1
    private var checkedItem = 1
    lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        mainViewModel = (activity as MainActivity).mainViewModel

        mainViewModel.getLocationData().observe(requireActivity(), Observer {
            val zoomLevel = 14f
            val latLong = LatLng(it.latitude, it.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, zoomLevel))

        })


        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val carModel =resources.getStringArray(R.array.carModel)


        binding.btnProblem.setOnClickListener {
            if (isPermissionGranted() && mainViewModel.isGpsOrNetworkEnabled()) {

                CoroutineScope(Dispatchers.IO).launch {
                    val alertDialog = AlertDialog.Builder(requireActivity())
                    alertDialog.setTitle("Car Model")
                    alertDialog.setCancelable(false)

                    withContext(Dispatchers.Main) {

                        alertDialog.setPositiveButton("Yes") { _, _ ->
                            Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show()
                        }

                        alertDialog.setNeutralButton("Cancel") { _, _ ->
                            Toast.makeText(activity, "You Cancel", Toast.LENGTH_LONG).show()
                        }

                        alertDialog.setSingleChoiceItems(carModel, checkedItem) { _, item ->
                            when (item) {
                                item -> {
                                    Toast.makeText(activity, carModel[item], Toast.LENGTH_LONG)
                                        .show()

                                }
                            }
                        }
                        val mAlertDialog = alertDialog.create()
                        withContext(Dispatchers.Main) {
                            mAlertDialog.show()
                        }

                    }

                }
            } else {
                requestLocationPermission()
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (isPermissionGranted()) {
            checkLocationSettings()
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
            Snackbar.make(
                binding.fragmentHome,
                "Please allow the location",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("ok") {
                    requestLocationPermission()
                }.show()
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
                binding.fragmentHome,
                "Please allow location permission",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Allow") {
                requestLocationPermission()
            }.show()

        } else {
            if (mainViewModel.isGpsOrNetworkEnabled()) {
                map.isMyLocationEnabled = true

            } else {
                checkLocationSettings()
            }
        }
    }


    @TargetApi(29)
    fun isPermissionGranted(): Boolean {

        val foregroundLocationPermission =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ))


        val backgroundLocationPermission = if (runningQorLater) {
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ))
        } else {
            true
        }

        return foregroundLocationPermission && backgroundLocationPermission

    }

    @TargetApi(29)
    fun requestLocationPermission() {
        if (isPermissionGranted())
            return checkLocationSettings()

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
            requireActivity(),
            permissionArray,
            resultCode
        )
    }

    @SuppressLint("MissingPermission")
    fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setNeedBle(true)
        val task: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            Log.d(TAG, "Location setting are enabled to true")

            if (isPermissionGranted()) {
                map.isMyLocationEnabled = true
            } else {
                requestLocationPermission()
            }
        }

        task.addOnFailureListener { exception ->

            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(activity, REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (e: ApiException) {
                    Log.d(TAG, "System fail to access your location")
                }
            } else {
                Snackbar.make(
                    binding.fragmentHome,
                    "Please Allow The Location Setting",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkLocationSettings()
                }.show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                when (resultCode) {
                    RESULT_OK -> mainViewModel.isGpsOrNetworkEnabled()
                    RESULT_CANCELED -> {
                        Log.d(TAG, "result cancelled");
                        Snackbar.make(
                            binding.fragmentHome,
                            "Allow Location",
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction("ok") {
                                checkLocationSettings()
                            }.show()
                    }

                }
            }
        }
    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_AND_BACKGROUND_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 999
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
