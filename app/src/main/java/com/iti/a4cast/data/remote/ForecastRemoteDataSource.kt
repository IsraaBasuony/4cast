package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ForecastRemoteDataSource private constructor() : IForecastRemoteDataSource {

    private val apiService:ApiService by lazy {
        RetrofitClient.getInstance().create(ApiService::class.java)
    }

    override fun getForecastWeather(lat:Double, lon:Double,lang:String):Flow<ForecastResponse> {
        return flow{emit(apiService.getForecastWeather(lat, lon, lang).body()!!)}
    }

    companion object{
        private var instance: ForecastRemoteDataSource? = null
        fun getInstance(): ForecastRemoteDataSource{
            return  instance?: synchronized(this){
                val temp = ForecastRemoteDataSource()
                instance = temp
                temp
            }
        }
    }
}