package com.iti.a4cast.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.repo.IForecastRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "Home"

class HomeViewModel (private val _iRepo: IForecastRepo) :ViewModel(){
    private val _forecastResponse : MutableStateFlow<WeatherStatus<ForecastResponse>> = MutableStateFlow(WeatherStatus.Loading)
    val forecastResponse = _forecastResponse


   fun getForecastWeather(lat:Double , lon:Double,lang:String  ){
        viewModelScope.launch(Dispatchers.IO) {
           try {
               _iRepo.getForecastWeather(lat, lon, lang).catch {e->
                   _forecastResponse.value = WeatherStatus.Error(e)

               }.collectLatest{
                   _iRepo.deleteLastForecast()
                       _forecastResponse.value = WeatherStatus.Success(it)
                   _iRepo.insertLastForecast(it)

               }
           }catch (e:IOException){
               _forecastResponse.value = WeatherStatus.Error(e)
           }
        }
    }

    fun getStoredForecast(){
        viewModelScope.launch (Dispatchers.IO){
            try {
                _iRepo.getLastForecast().catch {e->
                    _forecastResponse.value = WeatherStatus.Error(e)
                }.collectLatest {
                    if(it != null) {
                        _forecastResponse.value = WeatherStatus.Success(it)
                    }
                }
            }catch (e:IOException){
                _forecastResponse.value = WeatherStatus.Error(e)
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