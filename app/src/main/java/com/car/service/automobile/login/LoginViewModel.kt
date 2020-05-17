/**
 * written by robert arnold
 */

package com.car.service.automobile.login

import androidx.lifecycle.ViewModel
import com.car.service.automobile.repository.FirebaseInstance

class LoginViewModel(private val firebaseInstance: FirebaseInstance) : ViewModel() {

    suspend fun signUp(uid: String, name: String, phoneNumber: String) {
        firebaseInstance.signUp(uid, name, phoneNumber)
    }
}
