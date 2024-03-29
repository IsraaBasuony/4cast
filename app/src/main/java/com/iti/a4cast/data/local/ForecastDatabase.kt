package com.iti.a4cast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.model.FavLocation

@Database(entities = [FavLocation::class, AlertModel::class], version = 2, exportSchema = false)
abstract class ForecastDatabase: RoomDatabase() {

    abstract  fun forecastDao(): ForecastDao

    companion object{
        @Volatile
        private var INSTANCE: ForecastDatabase? = null

        fun getInstance(ctx: Context): ForecastDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(ctx, ForecastDatabase::class.java, "weather_db")
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }


}