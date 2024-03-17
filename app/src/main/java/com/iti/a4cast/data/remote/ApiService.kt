package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query
interface ApiService {

    @GET("onecall")
    suspend fun getForecastWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") appid: String,
        //@Query("units") units: String,
       // @Query("lang") lang: String
    ): ForecastResponse
}


