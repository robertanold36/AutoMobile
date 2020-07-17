package com.car.service.automobile.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.car.service.automobile.R
import com.car.service.automobile.main.ui.MainActivity
import com.car.service.automobile.main.MainViewModel

/**
 * A simple [Fragment] subclass.
 */
class RequestFragment : Fragment() {
    lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel = (activity as MainActivity).mainViewModel

    }

}
