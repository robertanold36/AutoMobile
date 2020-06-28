package com.car.service.automobile.model

data class WorkShopResponse(
    val CompanyReview: Int,
    val VehicleBrands: List<String>,
    val _id: String,
    val contacts: String,
    val location: LocationX,
    val name: String,
    val service: List<String>,
    val workshopClass: String
)