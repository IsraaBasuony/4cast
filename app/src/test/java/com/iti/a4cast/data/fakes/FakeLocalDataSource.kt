package com.iti.a4cast.data.fakes

import com.iti.a4cast.data.local.ILocalDatasource
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLocalDataSource(
    private val alerts: MutableList<AlertModel> = mutableListOf(),
    private val favLocations: MutableList<FavLocation> = mutableListOf()
) : ILocalDatasource {
    override fun getAllFavLocations(): Flow<List<FavLocation>> {
        return flowOf(favLocations.toList())
    }

    override fun insertFavLocation(favLocation: FavLocation) {
        favLocations.add(favLocation)
    }

    override fun deleteFavLocation(favLocation: FavLocation) {
        favLocations.remove(favLocation)
    }

    override fun getAllAlerts(): Flow<List<AlertModel>> {
        return flowOf(alerts.toList())
    }

    override suspend fun insertAlert(alertModel: AlertModel) {
        alerts.add(alertModel)
    }

    override suspend fun deleteAlert(alertModel: AlertModel) {
        alerts.remove(alertModel)
    }

    override fun getAlertByID(id: String): AlertModel {
        return alerts.first { it.id == id }
    }

    override fun insertLastForecast(forecastResponse: ForecastResponse) {
        TODO("Not yet implemented")
    }

    override fun getLastForecast(): Flow<ForecastResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLastForecast() {
        TODO("Not yet implemented")
    }
}