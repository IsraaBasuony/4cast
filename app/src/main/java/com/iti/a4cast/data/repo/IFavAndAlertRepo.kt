package com.iti.a4cast.data.repo

import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

interface IFavAndAlertRepo {
    fun getAllFavLocations(): Flow<List<FavLocation>>
    fun insertFavLocation(favLocation: FavLocation)
    fun deleteFavLocation(favLocation: FavLocation)
    fun getAllAlerts(): Flow<List<AlertModel>>
    suspend fun insertAlert(alertModel: AlertModel)
    suspend fun deleteAlert(alertModel: AlertModel)
    fun getAlertByID(id: String): AlertModel
}