package com.iti.a4cast.data.remote

import com.iti.a4cast.data.model.ForecastResponse

interface IForecastRemoteDataSource {
     suspend fun getForecastWeather(): ForecastResponse
}