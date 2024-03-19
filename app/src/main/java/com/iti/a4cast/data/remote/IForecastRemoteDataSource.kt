package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface IForecastRemoteDataSource {
     suspend fun getForecastWeather(lat:Double, lon:Double,unit:String,lang:String): Flow<ForecastResponse>
}