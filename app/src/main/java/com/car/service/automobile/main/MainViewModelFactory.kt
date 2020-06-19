package com.car.service.automobile.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.repository.ApiRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val apiRepository: ApiRepository, val app:Application):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(apiRepository,app) as T
    }

}