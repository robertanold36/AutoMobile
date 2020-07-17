package com.car.service.automobile.utility

import com.car.service.automobile.model.WorkShopResponseX


interface Listener {
    fun onLoading()
    fun onError(message:String)
    fun onSuccess(data: WorkShopResponseX)
}