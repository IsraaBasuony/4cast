package com.iti.a4cast.data.repo

import com.iti.a4cast.data.local.ILocalDatasource
import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

class FavAndAlertRepo private constructor(private var _localDatasource: ILocalDatasource) :
    IFavAndAlertRepo {

    companion object {
        private var instance: FavAndAlertRepo? = null
        fun getInstant(localDatasource: LocalDatasource):FavAndAlertRepo{
            return instance ?: synchronized(this) {
                val temp = FavAndAlertRepo(localDatasource)
                instance = temp
                temp
            }
        }
    }

    override fun getAllFavLocations():Flow<List<FavLocation>>{
        return  _localDatasource.getAllFavLocations()
    }

    override fun insertFavLocation(favLocation: FavLocation){
        _localDatasource.insertFavLocation(favLocation)
    }

    override fun deleteFavLocation(favLocation: FavLocation){
        _localDatasource.deleteFavLocation(favLocation)
    }

    override fun getAllAlerts(): Flow<List<AlertModel>> {
        return _localDatasource.getAllAlerts()
    }

    override suspend fun insertAlert(alertModel: AlertModel) {
        _localDatasource.insertAlert(alertModel)
    }

    override suspend fun deleteAlert(alertModel: AlertModel) {
        _localDatasource.deleteAlert(alertModel)
    }

    override fun getAlertByID(id: String): AlertModel {
        return _localDatasource.getAlertByID(id)
    }
}