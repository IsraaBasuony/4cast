package com.iti.a4cast.data.repo

import com.iti.a4cast.data.local.LocalDatasource
import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

class FavLocationsRepo private constructor( private var localDatasource: LocalDatasource) :
    IFavLocationsRepo {

    companion object {
        private var instance: FavLocationsRepo? = null
        fun getInstant(localDatasource: LocalDatasource):FavLocationsRepo{
            return instance ?: synchronized(this) {
                val temp = FavLocationsRepo(localDatasource)
                instance = temp
                temp
            }
        }
    }

    override fun getAllFavLocations():Flow<List<FavLocation>>{
        return  localDatasource.getAllFavLocations()
    }

    override fun insertFavLocation(favLocation: FavLocation){
        localDatasource.insertFavLocation(favLocation)
    }

    override fun deleteFavLocation(favLocation: FavLocation){
        localDatasource.deleteFavLocation(favLocation)
    }
}