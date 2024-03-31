package com.iti.a4cast.ui.alert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.repo.IFavAndAlertRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertViewModel(private val _repo: IFavAndAlertRepo) : ViewModel() {

    private val _alerts: MutableStateFlow<List<AlertModel>> =MutableStateFlow(listOf())
    var alerts = _alerts


    fun getAllAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getAllAlerts().collectLatest {
                _alerts.value = it
            }
        }
    }

    fun insertAlert(data: AlertModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.insertAlert(data)
            getAllAlerts()
        }
    }

    fun deleteAlert(data: AlertModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteAlert(data)
            getAllAlerts()
        }
    }

}