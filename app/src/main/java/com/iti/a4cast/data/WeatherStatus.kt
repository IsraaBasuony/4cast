package com.iti.a4cast.data

sealed class WeatherStatus<out T> {
    data class Success<out T>(val data: T) : WeatherStatus<T>()
    data class Error(val exception: Throwable) : WeatherStatus<Nothing>()
    data object Loading : WeatherStatus<Nothing>()
}