package com.car.service.automobile.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.car.service.automobile.R
import com.car.service.automobile.databinding.FragmentHomeBinding
import com.car.service.automobile.main.MainActivity
import com.car.service.automobile.main.MainViewModel


class HomeFragment : Fragment() {

    lateinit var mainViewModel: MainViewModel
    private val TAG = "MainActivity"
    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        mainViewModel = (activity as MainActivity).mainViewModel

        return binding.root

    }
}

