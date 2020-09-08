package com.car.service.automobile.api

import com.car.service.automobile.model.GarageResult
import com.car.service.automobile.model.PushNotification
import com.car.service.automobile.model.WorkShopResponse
import com.car.service.automobile.utility.Constants.Companion.CONTENT_TYPE
import com.car.service.automobile.utility.Constants.Companion.SERVER_KEY
import com.squareup.okhttp.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NearGarageApi {

    @GET("api/nearby")
    suspend fun getNearbyGarage(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double
    ):Response<GarageResult>

    @GET("workshop/requestWorkshop")
    suspend fun requestWorkShop(
        @Query("workshopID") workshopID:String
    ):Response<WorkShopResponse>

    @Headers("Authorization:key=$SERVER_KEY","Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ):Response<ResponseBody>


}