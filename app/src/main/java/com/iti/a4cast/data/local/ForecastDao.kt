package com.iti.a4cast.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface ForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLastForecast(forecastResponse: ForecastResponse)

    @Query("SELECT * FROM Forecast")
    fun getLastForecast():Flow<ForecastResponse>

    @Query("DELETE FROM Forecast")
    suspend fun deleteLastForecast()

    @Query("select * from fav_locations")
    fun getAllFavLocations(): Flow<List<FavLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = FavLocation::class)

    fun insertFavLocation(favLocation: FavLocation)

    @Delete(entity = FavLocation::class)
    fun deleteFavLocation(favLocation: FavLocation)

    @Query("select * from AlertModel")
    fun getAllAlerts(): Flow<List<AlertModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AlertModel::class)
    fun insertAlert(alerts: AlertModel)

    @Delete(entity = AlertModel::class)
    fun deleteAlert(alerts: AlertModel)

    @Query("select * from AlertModel where id = :id")
    fun getAlertByID(id: String): AlertModel

}