package com.car.service.automobile.api

import com.car.service.automobile.utility.Constants.Companion.BASE_URL
import com.car.service.automobile.utility.Constants.Companion.BASE_URL_FIREBASE
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GarageNearbyInstance {

    companion object{

        private fun createApiInstance(baseUrl:String):Retrofit{

            val retrofit:Retrofit by lazy {
                val loggingInterceptor=HttpLoggingInterceptor()
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()

                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            }

            return retrofit
        }

        val api by lazy {

            createApiInstance(BASE_URL).create(NearGarageApi::class.java)
        }

        val notificationApi by lazy {
            createApiInstance(BASE_URL_FIREBASE).create(NearGarageApi::class.java)
        }
    }
}