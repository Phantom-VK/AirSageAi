package com.vikram.airsageai.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vikram.airsageai.data.dataclass.AirQualityRequest
import com.vikram.airsageai.data.dataclass.AirQualityResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface AirQualityApiService {
    @POST
    suspend fun getCurrentConditions(
        @Url fullUrl: String,
        @Body request: AirQualityRequest
    ): Response<AirQualityResponse>
}

object RetrofitInstance {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://airquality.googleapis.com/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: AirQualityApiService = retrofit.create(AirQualityApiService::class.java)
}




