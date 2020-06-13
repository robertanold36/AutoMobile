package com.car.service.automobile.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application):AndroidViewModel(application) {

    private val locationUpdate=LocationTracking(application)

    fun getLocationData()=locationUpdate
}