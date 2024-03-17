package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse

class ForecastRemoteDataSource private constructor() : IForecastRemoteDataSource {

    private val apiService:ApiService by lazy {
        RetrofitClient.getInstance().create(ApiService::class.java)
    }

    override suspend fun getForecastWeather(): ForecastResponse {
        return apiService.getForecastWeather(44.34,10.99,"fa7cdd984905233c994258346ab7d49c")
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