package com.iti.a4cast.ui.favourite.viewmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.model.FavLocation
import com.iti.a4cast.data.repo.IFavAndAlertRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavouriteViewModel (private  val _repo: IFavAndAlertRepo): ViewModel() {

    private val _favLocations : MutableStateFlow<List<FavLocation>> = MutableStateFlow(listOf())
    var favLocations = _favLocations


    fun getAllFavLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getAllFavLocations().collectLatest{
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