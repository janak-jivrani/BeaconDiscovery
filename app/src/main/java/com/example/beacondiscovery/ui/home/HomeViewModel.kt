package com.example.beacondiscovery.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.beacondiscovery.api.ApiService
import com.example.beacondiscovery.api.RetrofitClient
import com.example.beacondiscovery.models.BeaconsCsvDataModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    val postsLiveData = MutableLiveData<Any>()
    val errorLiveData = MutableLiveData<String>()

    fun sendBeacons(url: String, dataModel: BeaconsCsvDataModel) {

        val apiService = RetrofitClient().retrofit.create(ApiService::class.java)

        val call: Call<Any> = apiService.sendBeacons(url = url, dataModel = dataModel)

        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val posts = response.body()
                    postsLiveData.postValue(posts)
                } else {
                    errorLiveData.postValue("Api Calling Error"+response.errorBody())
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                errorLiveData.postValue("Network error"+t.message)
            }
        })
    }

}