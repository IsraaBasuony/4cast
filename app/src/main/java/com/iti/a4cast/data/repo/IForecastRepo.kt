package com.iti.a4cast.data.repo

import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface IForecastRepo {
    fun getForecastWeather(
        lat: Double,
        lon: Double,
        lang: String
    ): Flow<ForecastResponse>

    fun getLanguage(): String

    fun setLanguage(language: String)
}