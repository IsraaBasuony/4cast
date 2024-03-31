package com.iti.a4cast.data.fakes

import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.IFavAndAlertRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeFavAndAlertsRepo: IFavAndAlertRepo {
    private val alerts: MutableList<AlertModel> = mutableListOf()
    private val favLocations: MutableList<FavLocation> = mutableListOf()


    override fun getAllFavLocations(): Flow<List<FavLocation>> {
        return flow { emit(favLocations) }
    }

    override fun insertFavLocation(favLocation: FavLocation) {
        favLocations.add(favLocation)
    }

    override fun deleteFavLocation(favLocation: FavLocation) {
        favLocations.remove(favLocation)
    }

    override fun getAllAlerts(): Flow<List<AlertModel>> {
        return flow { emit(alerts) }
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
}