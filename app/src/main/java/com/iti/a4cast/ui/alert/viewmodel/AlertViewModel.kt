package com.iti.a4cast.ui.alert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.a4cast.data.WeatherStatus
import com.iti.a4cast.data.model.AlertModel
import com.iti.a4cast.data.repo.IFavAndAlertRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertViewModel(private val _repo: IFavAndAlertRepo) : ViewModel() {

    private val _alerts: MutableStateFlow<WeatherStatus<List<AlertModel>>> = MutableStateFlow(WeatherStatus.Loading)
    val alerts = _alerts.asStateFlow()


    fun getAllAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getAllAlerts().collectLatest {
                _alerts.value = WeatherStatus.Success(it)
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