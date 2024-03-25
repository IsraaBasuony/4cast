package com.iti.a4cast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iti.a4cast.data.model.FavLocation

@Database(entities = arrayOf(FavLocation::class), version = 1)
abstract class WeatherDatabase: RoomDatabase() {

    abstract  fun getFavLocationDao(): FavLocationDao

    companion object{
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(ctx: Context): WeatherDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(ctx, WeatherDatabase::class.java, "weather_db").build()
                INSTANCE = instance
                instance
            }
        }
    }


}