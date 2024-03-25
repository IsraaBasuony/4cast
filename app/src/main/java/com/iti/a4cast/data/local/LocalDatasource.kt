package com.iti.a4cast.data.local

import android.content.Context
import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

class LocalDatasource private constructor(val context: Context) :
    ILocalDatasource {
    private val dao: FavLocationDao

    init {
        val db = WeatherDatabase.getInstance(context.applicationContext)
        dao = db.getFavLocationDao()
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

    companion object{
        private var instance: LocalDatasource? = null
        fun getInstance(context: Context): LocalDatasource {
            return  instance?: synchronized(this){
                val temp = LocalDatasource(context)
                instance = temp
                temp
            }
        }
    }
}