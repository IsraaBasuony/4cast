package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
interface ApiService {

    @GET("onecall?appid=fa7cdd984905233c994258346ab7d49c")
    suspend fun getForecastWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Response<ForecastResponse>
}


