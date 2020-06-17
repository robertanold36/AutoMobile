package com.car.service.automobile.main


import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.car.service.automobile.Resource
import com.car.service.automobile.SystemApplication
import com.car.service.automobile.model.GarageResult
import com.car.service.automobile.repository.ApiRepository
import com.car.service.automobile.utility.LocationEventListener
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(val apiRepository: ApiRepository, app: Application) :
    AndroidViewModel(app) {

    private val locationUpdate = LocationTracking(app)
    val garageList:MutableLiveData<Resource<GarageResult>> = MutableLiveData()
    var garageListResponse:GarageResult?=null

    fun getLocationData() = locationUpdate


    private val TAG = "MainActivity"

    var gps_enabled = false
    var network_enabled = false



    lateinit var locationListener: LocationEventListener

    fun isGpsOrNetworkEnabled(): Boolean {

        val locationManager =
            getApplication<SystemApplication>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        } catch (e: Exception) {
        }
        return gps_enabled && network_enabled
    }


     fun getAllNearByGarage(lat: Double, lon: Double)=viewModelScope.launch{
         garageList.postValue(Resource.Loading())
         val response=apiRepository.getAllNearbyGarage(lat,lon)
         garageList.postValue(handleGarageListResult(response))
    }

    private fun handleGarageListResult(response:Response<GarageResult>):Resource<GarageResult>{
        if(response.isSuccessful){
            response.body().let {
                garageListResponse=it
               return Resource.Success(garageListResponse)
            }
        }else{
            return Resource.Error("Fail to retrieve data")
        }
    }



}