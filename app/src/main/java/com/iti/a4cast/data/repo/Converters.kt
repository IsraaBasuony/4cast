package com.iti.a4cast.data.repo

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iti.a4cast.data.model.Alerts
import com.iti.a4cast.data.model.Current
import com.iti.a4cast.data.model.Daily
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.model.Weather

class Converters {
    @TypeConverter
    fun fromCurrentToString(current: Current?) = Gson().toJson(current)
    @TypeConverter
    fun fromStringToCurrent(stringCurrent : String?) = Gson().fromJson(stringCurrent, Current::class.java)

    @TypeConverter
    fun fromWeatherToString(weather: List<Weather>) = Gson().toJson(weather)
    @TypeConverter
    fun fromStringToWeather(stringCurrent : String) = Gson().fromJson(stringCurrent, Array<Weather>::class.java).toList()

    @TypeConverter
    fun fromDailyListToString(daily: List<Daily>) = Gson().toJson(daily)
    @TypeConverter
    fun fromStringToDailyList(stringDaily : String) = Gson().fromJson(stringDaily, Array<Daily>::class.java).toList()

    @TypeConverter
    fun fromHourlyListToString(hourly: List<Current>) = Gson().toJson(hourly)
    @TypeConverter
    fun fromStringToHourlyList(stringHourly : String) = Gson().fromJson(stringHourly, Array<Current>::class.java).toList()

    @TypeConverter
    fun fromAlertsToString(alerts: List<Alerts>?): String {
        if (!alerts.isNullOrEmpty()) {
            return Gson().toJson(alerts)
        }
        return ""
    }
    @TypeConverter
    fun fromStringToAlerts(alerts: String?): List<Alerts> {
        if (alerts.isNullOrEmpty()) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Alerts?>?>() {}.type
        return Gson().fromJson(alerts, listType)
    }
    @TypeConverter
    fun fromWelcomeToString(root: ForecastResponse) = Gson().toJson(root)
    @TypeConverter
    fun fromStringToWelcome(stringCurrent : String) = Gson().fromJson(stringCurrent, ForecastResponse::class.java)


}