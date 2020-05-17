package com.car.service.automobile.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.car.service.automobile.repository.FirebaseInstance

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(private val firebaseInstance: FirebaseInstance): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(firebaseInstance) as T
    }
}