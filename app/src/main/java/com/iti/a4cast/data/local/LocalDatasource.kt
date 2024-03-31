package com.iti.a4cast.data.local

import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow

class LocalDatasource private constructor(private val dao: ForecastDao) :
    ILocalDatasource {


    override fun getAllFavLocations(): Flow<List<FavLocation>> {
        return dao.getAllFavLocations()
    }

    override fun insertFavLocation(favLocation: FavLocation) {
        dao.insertFavLocation(favLocation)
    }

    override fun deleteFavLocation(favLocation: FavLocation) {
        dao.deleteFavLocation(favLocation)
    }

    override fun getAllAlerts(): Flow<List<AlertModel>> {
        return dao.getAllAlerts()
    }

    override suspend fun insertAlert(alertModel: AlertModel) {
        dao.insertAlert(alertModel)
    }

    override suspend fun deleteAlert(alertModel: AlertModel) {
       dao.deleteAlert(alertModel)
    }

    override fun getAlertByID(id: String):AlertModel {
        return  dao.getAlertByID(id)
    }

    override fun insertLastForecast(forecastResponse: ForecastResponse) {
        dao.insertLastForecast(forecastResponse)
    }

    override fun getLastForecast(): Flow<ForecastResponse> {
        return dao.getLastForecast()
    }

    override suspend fun deleteLastForecast() {
        dao.deleteLastForecast()
    }

    companion object {
        private var instance: LocalDatasource? = null
        fun getInstance( dao: ForecastDao): LocalDatasource {
            return instance ?: synchronized(this) {
                val temp = LocalDatasource(dao)
                instance = temp
                temp
            }
        }
    }
}