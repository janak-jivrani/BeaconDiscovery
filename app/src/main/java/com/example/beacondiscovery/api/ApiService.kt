package com.example.beacondiscovery.api

import com.example.beacondiscovery.models.BeaconsCsvDataModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {

    @POST
    fun sendBeacons(
        @Url url: String,
        @Header("Ocp-Apim-Subscription-Key") authToken: String = "6d3e7466eb284805985272dc4b18e09e",
        @Header("Content-Type") contentType: String = "application/json",
        @Body dataModel: BeaconsCsvDataModel
    ): Call<Any>

}