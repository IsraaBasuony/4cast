package com.iti.a4cast.data.repo

import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

interface IFavLocationsRepo {
    fun getAllFavLocations(): Flow<List<FavLocation>>
    fun insertFavLocation(favLocation: FavLocation)
    fun deleteFavLocation(favLocation: FavLocation)
}