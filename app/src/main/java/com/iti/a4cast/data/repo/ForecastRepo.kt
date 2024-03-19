package com.iti.a4cast.data.repo

import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import kotlinx.coroutines.flow.Flow

class ForecastRepo private constructor(private var forecastRemoteDataSource: ForecastRemoteDataSource) :
    IForecastRepo {
    companion object{
        private var instance: ForecastRepo? = null

        fun getInstant(forecastRemoteDataSource: ForecastRemoteDataSource): ForecastRepo{
            return  instance?: synchronized(this){
                val temp = ForecastRepo(forecastRemoteDataSource)
                instance = temp
                temp
            }
        }
    }

    override  suspend fun getForecastWeather(lat:Double, lon:Double,unit:String,lang:String): Flow<ForecastResponse> {
        return forecastRemoteDataSource.getForecastWeather(lat, lon, unit, lang)
    }

}