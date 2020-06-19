package com.car.service.automobile.main.ui


import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import com.car.service.automobile.R
import com.car.service.automobile.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashScreen : AppCompatActivity() {

    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            startActivity(Intent(this@SplashScreen,MainActivity::class.java))
            finish()
        }
    }
}




