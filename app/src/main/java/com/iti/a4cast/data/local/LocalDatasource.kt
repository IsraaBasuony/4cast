package com.iti.a4cast.data.local

import android.content.Context
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

class LocalDatasource private constructor(val context: Context) :
    ILocalDatasource {
    private val dao: ForecastDao

    init {
        val db = ForecastDatabase.getInstance(context.applicationContext)
        dao = db.forecastDao()
    }

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

    companion object {
        private var instance: LocalDatasource? = null
        fun getInstance(context: Context): LocalDatasource {
            return instance ?: synchronized(this) {
                val temp = LocalDatasource(context)
                instance = temp
                temp
            }
        }
    }
}