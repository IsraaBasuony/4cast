package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface IForecastRemoteDataSource {
     fun getForecastWeather(lat:Double, lon:Double,lang:String): Flow<ForecastResponse>
}