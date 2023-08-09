package com.example.beacondiscovery.api

import okhttp3.FormBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {

    private val BASE_URL = "https://api.example.com/"

    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            println("Request URL: ${request.url()}")
            println("Request Method: ${request.method()}")
            println("Request Headers: ${request.headers()}")
            val requestBody = request.body()
            if (requestBody != null) {
                println("Request Body:")
                if (requestBody is FormBody) {
                    for (i in 0 until requestBody.size()) {
                        println("${requestBody.name(i)}: ${requestBody.value(i)}")
                    }
                } else {
                    println(requestBody.toString())
                }
            }

            // Log response details
            println("Response Code: ${response.code()}")
            println("Response Message: ${response.message()}")
            println("Response Headers: ${response.headers()}")

            // Log request and response details here if needed
            response
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

}