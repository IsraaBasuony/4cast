package com.iti.a4cast.data.repo

import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface IForecastRepo {
   suspend fun getForecastWeather(lat:Double, lon:Double,unit:String,lang:String): Flow<ForecastResponse>
}