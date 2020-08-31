package com.car.service.automobile.main


import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.car.service.automobile.SystemApplication
import com.car.service.automobile.model.GarageResult
import com.car.service.automobile.model.PushNotification
import com.car.service.automobile.model.UserPOJO
import com.car.service.automobile.model.WorkShopResponse
import com.car.service.automobile.repository.ApiRepository
import com.car.service.automobile.utility.Constants
import com.car.service.automobile.utility.Listener
import com.car.service.automobile.utility.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException

class MainViewModel(private val apiRepository: ApiRepository, app: Application) :
    AndroidViewModel(app) {

    private val locationUpdate = LocationTracking(app)
    val garageList: MutableLiveData<Resource<GarageResult>> = MutableLiveData()
    var garageListResponse: GarageResult? = null
    lateinit var listener: Listener
    val userDetail:MutableLiveData<UserPOJO> = MutableLiveData()
    private val fStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private var job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)
    private val TAG = "MainActivity"
    private var gpsEnabled = false
    private var networkEnabled = false


    fun getLocationData() = locationUpdate

    fun getUserDetail(uid: String) {
        ioScope.launch {
            fStore.collection(Constants.COLLECTION_CLIENT).document(uid).get()
                .addOnSuccessListener {
                    val uid = it.get("uid").toString()
                    val name = it.get("name").toString()
                    val phoneNumber = it.get("phoneNumber").toString()

                    val userPOJO = UserPOJO(uid, name, phoneNumber)
                    Log.e("MainActivity", userPOJO.toString())
                    userDetail.value = userPOJO
                }
        }
    }

    fun sendNotification(pushNotification: PushNotification) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (isConnectedToInternet()) {
                    val response = apiRepository.sendNotification(pushNotification)
                    if (response.isSuccessful) {
                        Log.e("MainActivity", "success")
                    } else {
                        Log.e("MainActivity", "fail")

                    }
                } else {

                }
            } catch (e: Exception) {

            }
        }

    fun getAllNearByGarage(lat: Double, lon: Double) = viewModelScope.launch(Dispatchers.Main) {
        garageList.postValue(Resource.Loading())
        try {
            if (isConnectedToInternet()) {
                withContext(Dispatchers.IO) {
                    val response = apiRepository.getAllNearbyGarage(lat, lon)
                    withContext(Dispatchers.Main){
                        garageList.postValue(handleGarageListResult(response))
                    }
                }
            } else {

                garageList.postValue(Resource.Error("No Internet Connection"))

            }
        } catch (t: Throwable) {
            when (t) {

                is IOException -> {
                    garageList.postValue(Resource.Error("Network Failure"))
                }
                else -> {
                    garageList.postValue(Resource.Error("Conversion Error"))
                }
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

    fun requestWorkshop(workshopID: String) = viewModelScope.launch(Dispatchers.Main) {

        listener.onLoading()
        try {
            if (isConnectedToInternet()) {
                withContext(Dispatchers.IO) {
                    val response = apiRepository.getNearByWorkshop(workshopID)
                    withContext(Dispatchers.Main) {
                        handleWorkshopResponse(response)
                    }
                }

            } else {

                listener.onError("No Internet connection")
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> listener.onError("Network Failure")
                else -> listener.onError("Internal server error")
            }
        }
    }

    private fun handleWorkshopResponse(response: Response<WorkShopResponse>) {
        if (response.isSuccessful) {
            response.body().let {
                val workshopInformation = it?.WorkShopResponse
                workshopInformation?.get(0)?.let { it1 ->
                    listener.onSuccess(it1)
                }
                Log.e("MainActivity", it.toString())

            }
        } else {
            listener.onError("Fail to get workshop")
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

     fun isGpsOrNetworkEnabled(): Boolean {

        val locationManager =
            getApplication<SystemApplication>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Log.e(TAG,"Fail to access gps location $e")
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        } catch (e: Exception) {
            Log.e(TAG,"Fail to access network location $e")
        }
        return gpsEnabled && networkEnabled
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

}