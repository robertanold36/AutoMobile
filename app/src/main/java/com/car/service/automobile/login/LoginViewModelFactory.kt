package com.car.service.automobile.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.repository.ApiRepository

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(val app: Application, private val apiRepository: ApiRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(app, apiRepository) as T
    }
}