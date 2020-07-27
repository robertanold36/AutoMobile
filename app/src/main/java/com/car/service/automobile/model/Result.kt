package com.car.service.automobile.model

import java.io.Serializable

data class Result(
    val _id: String,
    val location: Location,
    val state: String,
    val workshopID: String,
    val VehicleBrands:List<String>

):Serializable