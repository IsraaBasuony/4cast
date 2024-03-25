package com.iti.a4cast.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iti.a4cast.data.model.FavLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavLocationDao {

    @Query("select * from fav_locations")
    fun getAllFavLocations(): Flow<List<FavLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = FavLocation::class)

    fun insertFavLocation(favLocation: FavLocation)

    @Delete(entity = FavLocation::class)
    fun deleteFavLocation(favLocation: FavLocation)


}