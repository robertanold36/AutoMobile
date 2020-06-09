package com.car.service.automobile.loginListener

interface LoginListener {
    fun onFail(message: String)
    fun onSuccess(phoneNumber: String, name: String)
}