package com.car.service.automobile.main.ui

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.car.service.automobile.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_information.*

class HomeActivity : AppCompatActivity() {

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        iconMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, iconMenu)
            popupMenu.menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
            popupMenu.show()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(fragment_information)
        bottomSheetBehavior.peekHeight = 80

        textView6.setOnClickListener {
            if(bottomSheetBehavior.state==BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED
            }else{
                bottomSheetBehavior.state=BottomSheetBehavior.STATE_COLLAPSED

            }
        }

    }
}