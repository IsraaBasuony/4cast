package com.iti.a4cast.ui.favourite.viewmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.IFavLocationsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavouriteViewModel (private  val _repo: IFavLocationsRepo): ViewModel() {

    private val _favLocations : MutableStateFlow<List<FavLocation>> = MutableStateFlow(listOf())
    val favLocations = _favLocations.asStateFlow()


    fun getAllFavLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getAllFavLocations().collect{
                _favLocations.value = it
            }
        }
    }


    fun insertFavLocation(data: FavLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.insertFavLocation(data)
            getAllFavLocations()
        }
    }

    fun deleteFavLocation(data: FavLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteFavLocation(data)
            getAllFavLocations()
        }
    }


}