package com.car.service.automobile.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.car.service.automobile.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardDirectly.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
        }
        cardEmergency.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()

        }
        cardKnown.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()

        }
        cardSpare.setOnClickListener {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()

        }
    }

}
