package com.iti.a4cast.data.repo

import android.util.Log
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.ui.settings.SettingsSharedPref
import kotlinx.coroutines.flow.Flow

class ForecastRepo private constructor(
    private var forecastRemoteDataSource: ForecastRemoteDataSource,
    private var settingsSharedPref: SettingsSharedPref
) :
    IForecastRepo {
    companion object {
        private var instance: ForecastRepo? = null

        fun getInstant(
            forecastRemoteDataSource: ForecastRemoteDataSource,
            settingsSharedPref: SettingsSharedPref
        ): ForecastRepo {
            return instance ?: synchronized(this) {
                val temp = ForecastRepo(forecastRemoteDataSource, settingsSharedPref)
                instance = temp
                temp
            }
        }
    }

    override fun getForecastWeather(
        lat: Double,
        lon: Double,
        lang: String
    ): Flow<ForecastResponse> {
        return forecastRemoteDataSource.getForecastWeather(lat, lon,  lang)
    }

    override fun getLanguage(): String {
        Log.i("Lang", "getLanguage:REPO ${settingsSharedPref.getLanguagePref()}")
        return settingsSharedPref.getLanguagePref()!!
    }

    override fun setLanguage(language: String) {
        settingsSharedPref.setLanguagePref(language)
    }

}