package com.car.service.automobile.utility

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
private val runningQorLater =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


class NetworkUtility {

    companion object{
        @TargetApi(29)
        fun isPermissionGranted(context: Context): Boolean {

            return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))

        }
    }

}