package com.car.service.automobile.main


import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.car.service.automobile.Resource
import com.car.service.automobile.SystemApplication
import com.car.service.automobile.model.GarageResult
import com.car.service.automobile.model.WorkShopResponse
import com.car.service.automobile.repository.ApiRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class MainViewModel(private val apiRepository: ApiRepository, app: Application) :
    AndroidViewModel(app) {

    private val locationUpdate = LocationTracking(app)
    val garageList: MutableLiveData<Resource<GarageResult>> = MutableLiveData()
    var garageListResponse: GarageResult? = null
    val workshop: MutableLiveData<Resource<WorkShopResponse>> = MutableLiveData()
    var workShopResponse: WorkShopResponse? = null


    fun getLocationData() = locationUpdate

    private var gpsEnabled = false
    private var networkEnabled = false


    fun isGpsOrNetworkEnabled(): Boolean {

        val locationManager =
            getApplication<SystemApplication>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        } catch (e: Exception) {
        }
        return gpsEnabled && networkEnabled
    }


    fun getAllNearByGarage(lat: Double, lon: Double) = viewModelScope.launch {
        garageList.postValue(Resource.Loading())
        try {
            if (isConnectedToInternet()) {
                val response = apiRepository.getAllNearbyGarage(lat, lon)
                garageList.postValue(handleGarageListResult(response))
            } else {
                garageList.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> garageList.postValue(Resource.Error("Network Failure"))
                else -> garageList.postValue(Resource.Error("Conversion Error"))
            }
        }

    }

    private fun handleGarageListResult(response: Response<GarageResult>): Resource<GarageResult> {
        if (response.isSuccessful) {
            response.body().let {
                garageListResponse = it
                return Resource.Success(garageListResponse)
            }
        } else {
            return Resource.Error("Fail to retrieve data")
        }
    }

    fun requestWorkshop(workshopID: String) = viewModelScope.launch {
        workshop.postValue(Resource.Loading())
        try {
            if (isConnectedToInternet()) {
                val response = apiRepository.getNearByWorkshop(workshopID)
                workshop.postValue(handleWorkshopResponse(response))

            } else {
                workshop.postValue(Resource.Error("No Internet connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> workshop.postValue(Resource.Error("Network Failure"))
                else -> workshop.postValue(Resource.Error("Internal server error"))
            }
        }
    }

    private fun handleWorkshopResponse(response: Response<WorkShopResponse>): Resource<WorkShopResponse> {
        if(response.isSuccessful){
            response.body().let {
               workShopResponse=it
                return Resource.Success(workShopResponse)
            }
        }else{

            return Resource.Error("Fail to get workshop")
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getApplication<SystemApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false

                }
            }
        }
        return false
    }

}