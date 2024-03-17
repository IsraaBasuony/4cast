package com.iti.a4cast.data.model

data class ForecastResponse(
    val current: Current? = null,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val alerts: List<Alerts>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezoneOffset: Int?
) {

    data class Current(
        val dt: Long,
        val sunrise: Long? = null,
        val sunset: Long? = null,
        val temp: Double,
        val feelsLike: Double,
        val pressure: Long,
        val humidity: Long,
        val dewPoint: Double,
        val uvi: Double,
        val clouds: Long,
        val visibility: Long,
        val windSpeed: Double,
        val windDeg: Long,
        val windGust: Double,
        val weather: List<Weather>,
        val pop: Double? = null

    )

    data class Daily(
        val dt: Long,
        val sunrise: Long,
        val sunset: Long,
        val moonrise: Long,
        val moonset: Long,
        val moonPhase: Double,
        val temp: Temp,
        val rain: Double?,
        val feelsLike: FeelsLike,
        val pressure: Long,
        val humidity: Long,
        val dewPoint: Double,
        val windSpeed: Double,
        val windDeg: Long,
        val windGust: Double,
        val weather: List<Weather>,
        val clouds: Long,
        val pop: Double,
        val uvi: Double


    )

    data class FeelsLike(
        val day: Double,
        val night: Double,
        val eve: Double,
        val morn: Double
    )

    data class Temp(
        val day: Double,
        val min: Double,
        val max: Double,
        val night: Double,
        val eve: Double,
        val morn: Double
    )

    data class Weather(
        val description: String?,
        val icon: String?,
        val id: Int?,
        val main: String?
    )

    data class Hourly(
        val clouds: Int?,
        val dewPoint: Double?,
        val dt: Int?,
        val feelsLike: Double?,
        val humidity: Int?,
        val pop: Double?,
        val pressure: Int?,
        val temp: Double?,
        val uvi: Double?,
        val visibility: Int?,
        val weather: List<Weather>,
        val windDeg: Int?,
        val windGust: Double?,
        val windSpeed: Double?
    )

    data class Alerts(
        var senderName: String? = null,
        var event: String? = null,
        var start: Long? = null,
        var end: Long? = null,
        var description: String? = null,
        var tags: List<String>
    )
}

