/**
 * written by robert arnold
 */

/**
 * Login repository for register the user to the cloud firestore
 */

package com.car.service.automobile.repository

import com.car.service.automobile.api.GarageNearbyInstance
import com.car.service.automobile.api.GarageNearbyInstance.Companion.notificationApi
import com.car.service.automobile.model.PushNotification
import com.car.service.automobile.model.UserPOJO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ApiRepository {

    private val fStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val collectionPath = "clients"


    suspend fun signUp(uid: String, name: String, phoneNumber: String) {
        val user =
            UserPOJO(uid, name, phoneNumber)
        fStore.collection(collectionPath).document(uid).set(user).await()
    }

    suspend fun getAllNearbyGarage(lat: Double, lon: Double) = GarageNearbyInstance.api
        .getNearbyGarage(lat, lon)

    suspend fun getNearByWorkshop(workshopID: String) =
        GarageNearbyInstance.api.requestWorkShop(workshopID)

    suspend fun sendNotification(pushNotification: PushNotification) =
        notificationApi.postNotification(pushNotification)

}