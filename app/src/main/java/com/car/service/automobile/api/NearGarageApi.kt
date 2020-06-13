package com.car.service.automobile.api

import com.car.service.automobile.model.Garage
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NearGarageApi {


    @GET("api/nearby")
    suspend fun getNearbyGarage(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double
    ):Response<Garage>


}