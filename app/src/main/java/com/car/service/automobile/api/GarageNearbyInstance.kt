package com.car.service.automobile.api

import com.car.service.automobile.utility.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GarageNearbyInstance {

    companion object{
        private val retrofit:Retrofit by lazy {
            val loggingInterceptor=HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api by lazy {
            retrofit.create(NearGarageApi::class.java)
        }
    }
}