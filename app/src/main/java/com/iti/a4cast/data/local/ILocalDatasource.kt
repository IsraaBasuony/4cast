package com.iti.a4cast.data.local

import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

interface ILocalDatasource {

    fun getAllFavLocations(): Flow<List<FavLocation>>

    fun insertFavLocation(favLocation: FavLocation)

    fun deleteFavLocation(favLocation: FavLocation)

}