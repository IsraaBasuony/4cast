package com.iti.a4cast.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.model.ForecastResponse
import com.iti.a4cast.data.repo.IForecastRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "Home"

class HomeViewModel (private val _iRepo: IForecastRepo) :ViewModel(){
    private val _forecastResponse = MutableLiveData<ForecastResponse>()
    val forecastResponse : LiveData<ForecastResponse> = _forecastResponse
    init {
        Log.i(TAG, "instance initializer: Creation of ViewModel")
        getForecastWeather()
    }

   fun getForecastWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            val forecastResponse = _iRepo.getForecastWeather()
            withContext(Dispatchers.Main){
                Log.i(TAG, "getForecastWeather: ${forecastResponse.daily.get(0)}")
                _forecastResponse.postValue(forecastResponse)
            }
        }
    }

}