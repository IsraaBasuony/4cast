package com.iti.a4cast.data.repo

import com.iti.a4cast.data.model.ForecastResponse

interface IForecastRepo {
   suspend fun getForecastWeather(): ForecastResponse
}