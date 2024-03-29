package com.iti.a4cast.ui.favourite.viewmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.a4cast.data.repo.FavAndAlertRepo

class FavouriteViewModelFactory(private val _iRepo: FavAndAlertRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FavouriteViewModel::class.java)) {
            FavouriteViewModel(_iRepo) as T
        } else {
            throw IllegalArgumentException("ViewModel Class not found")
        }
    }

}