package com.iti.a4cast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.repo.Converters

@Database(entities = [FavLocation::class, AlertModel::class, ForecastResponse::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ForecastDatabase: RoomDatabase() {

    abstract  fun forecastDao(): ForecastDao

    companion object{
        @Volatile
        private var INSTANCE: ForecastDatabase? = null

        fun getInstance(ctx: Context): ForecastDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(ctx, ForecastDatabase::class.java, "weather_db")
                   .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }


}