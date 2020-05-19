package com.car.service.automobile.resources

interface LoginListener {
    fun onFail(message:String)
    fun onSuccess(phoneNumber:String,name:String)
}