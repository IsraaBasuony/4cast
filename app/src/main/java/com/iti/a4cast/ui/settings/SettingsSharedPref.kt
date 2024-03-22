package com.iti.a4cast.ui.settings


import android.content.Context
import android.content.SharedPreferences

private const val SETTINGS_SH_PREF = "SETTINGS_SHARED_PREFERENCES"
private const val TEMP_PREF = "TEMP_PREF"
private const val WIND_SPEED_PREF = "WIND_SPEED_PREF"
private const val LANGUAGE_PREF = "LANGUAGE_PREF"


class SettingsSharedPref private constructor(applicationContext: Context) {
    private var sharedPreferences: SharedPreferences =
        applicationContext.getSharedPreferences(SETTINGS_SH_PREF, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()


    fun setTempPref(tempPref: String) {
        editor.putString(TEMP_PREF, tempPref).apply()
    }

    fun getTempPref(): String? {
        return sharedPreferences.getString(TEMP_PREF, CELSIUS)
    }

    fun setLanguagePref(language: String) {
        editor.putString(LANGUAGE_PREF, language).apply()
    }

    fun getLanguagePref(): String {
        return sharedPreferences.getString(LANGUAGE_PREF, ENGLIGH)!!
    }

    fun setWindSpeedPref(windSpeedPref: String) {
        editor.putString(WIND_SPEED_PREF, windSpeedPref).apply()
    }

    fun getWindSpeedPref(): String? {
        return sharedPreferences.getString(WIND_SPEED_PREF, METER_PER_SECOND)
    }

    companion object {
        const val ARABIC = "ar"
        const val ENGLIGH = "en"

        const val METER_PER_SECOND = "METER_PER_SECOND"
        const val MILE_PER_HOUR = "MILE_PER_HOUR"

        const val CELSIUS = "CELSIUS"
        const val KELVIN = "KELVIN"
        const val FAHRENHEIT = "FAHRENHEIT"



        private var instance: SettingsSharedPref? = null

        fun getInstance(application: Context): SettingsSharedPref {
            return instance ?: synchronized(this) {
                val tempPref = SettingsSharedPref(application.applicationContext)
                instance = tempPref
                tempPref
            }

        }

    }


}

