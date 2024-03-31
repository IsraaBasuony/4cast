package com.iti.a4cast.data.repo

import android.util.Log
import com.iti.a4cast.data.local.ILocalDatasource
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.remote.ForecastRemoteDataSource
import com.iti.a4cast.data.remote.IForecastRemoteDataSource
import com.iti.a4cast.ui.settings.SettingsSharedPref
import kotlinx.coroutines.flow.Flow

class ForecastRepo private constructor(
    private var forecastRemoteDataSource: IForecastRemoteDataSource,
    private var localDatasource: ILocalDatasource,
    private var settingsSharedPref: SettingsSharedPref
) :
    IForecastRepo {
    companion object {
        private var instance: ForecastRepo? = null

        fun getInstant(
            forecastRemoteDataSource: ForecastRemoteDataSource,
            localDatasource: LocalDatasource,
            settingsSharedPref: SettingsSharedPref
        ): ForecastRepo {
            return instance ?: synchronized(this) {
                val temp = ForecastRepo(forecastRemoteDataSource,localDatasource, settingsSharedPref)
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

    override fun insertLastForecast(forecastResponse: ForecastResponse) {
        localDatasource.insertLastForecast(forecastResponse)
    }

    override fun getLastForecast(): Flow<ForecastResponse> {
        return localDatasource.getLastForecast()
    }

    override suspend fun deleteLastForecast() {
       localDatasource.deleteLastForecast()
    }

    override fun getLanguage(): String {
        Log.i("Lang", "getLanguage:REPO ${settingsSharedPref.getLanguagePref()}")
        return settingsSharedPref.getLanguagePref()!!
    }

    override fun setLanguage(language: String) {
        settingsSharedPref.setLanguagePref(language)
    }

}