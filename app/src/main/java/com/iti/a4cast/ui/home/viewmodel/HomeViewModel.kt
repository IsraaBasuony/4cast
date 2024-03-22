package com.iti.a4cast.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.repo.IForecastRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val TAG = "Home"

class HomeViewModel (private val _iRepo: IForecastRepo) :ViewModel(){
    private val _forecastResponse : MutableStateFlow<WeatherStatus<ForecastResponse>> = MutableStateFlow(WeatherStatus.Loading)
    val forecastResponse = _forecastResponse


   fun getForecastWeather(lat:Double , lon:Double,lang:String  ){
        viewModelScope.launch(Dispatchers.IO) {
            _iRepo.getForecastWeather(lat, lon, lang).collect{
                _forecastResponse.value = WeatherStatus.Success(it)
            }

        }
    }

    fun  getLanguage():String{
        return _iRepo.getLanguage()
    }

    fun setLanguage(language:String){
        _iRepo.setLanguage(language)
    }

}