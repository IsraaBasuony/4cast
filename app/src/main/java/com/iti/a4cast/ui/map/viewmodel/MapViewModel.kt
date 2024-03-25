package com.iti.a4cast.ui.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.IFavLocationsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel (private  val _repo: IFavLocationsRepo): ViewModel() {
    fun insertFavLocation(data: FavLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.insertFavLocation(data)
        }
    }

}