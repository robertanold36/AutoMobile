package com.car.service.automobile

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging

class SystemApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true;

    }
}