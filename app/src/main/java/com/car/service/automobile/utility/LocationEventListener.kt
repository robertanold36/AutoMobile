package com.car.service.automobile.utility

interface LocationEventListener {
    fun onSuccess()
    fun onFail(msg:String)
}