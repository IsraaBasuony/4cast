package com.iti.a4cast.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.a4cast.data.repo.ForecastRepo

class HomeViewModelFactory(private  val _iRepo: ForecastRepo): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(HomeViewModel::class.java)){
            HomeViewModel(_iRepo)as T
        }else{
            throw IllegalArgumentException("ViewModel Class not found")
        }
    }

}