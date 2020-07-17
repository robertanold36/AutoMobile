package com.car.service.automobile.model

data class WorkShopResponseX(

    val CompanyReview: Int,
    val VehicleBrands: List<String>,
    val _id: String,
    val contacts: String,
    val email: String,
    val location: LocationX,
    val name: String,
    val service: List<String>,
    val workshopClass: String
)