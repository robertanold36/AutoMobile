/**
 * written by robert arnold
 */

package com.car.service.automobile.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.car.service.automobile.repository.FirebaseInstance
import com.car.service.automobile.resources.LoginListener

class LoginViewModel(app: Application, private val firebaseInstance: FirebaseInstance) :
    AndroidViewModel(app) {

    var phoneNumber: String? = ""
    var name: String? = ""
    lateinit var listener: LoginListener

    suspend fun signUp(uid: String, name: String, phoneNumber: String) {
        firebaseInstance.signUp(uid, name, phoneNumber)
    }

    fun login() {
        if (phoneNumber.isNullOrEmpty() || name.isNullOrEmpty()) {
            listener.onFail("please fill the field to complete!!!")
        } else if (phoneNumber!!.length < 9) {
            listener.onFail("phone number is invalid!!")
        } else {
            listener.onSuccess(phoneNumber!!, name!!)
        }
    }


}
