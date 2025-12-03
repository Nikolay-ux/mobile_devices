package com.example.myapplication1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.model.MetalRate
import com.example.myapplication1.repository.MetalRepository
import kotlinx.coroutines.launch

class MetalViewModel : ViewModel() {

    private val repository = MetalRepository()

    private val _goldRate = MutableLiveData<MetalRate?>()
    val goldRate: LiveData<MetalRate?> = _goldRate

    private val _allMetals = MutableLiveData<List<MetalRate>>()
    val allMetals: LiveData<List<MetalRate>> = _allMetals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadGoldRate() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val rate = repository.getCurrentGoldRate()
                _goldRate.value = rate

                if (rate == null) {
                    _errorMessage.value = "Не удалось загрузить курс золота"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllMetalRates() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val rates = repository.getAllMetalRates()
                _allMetals.value = rates

                if (rates.isEmpty()) {
                    _errorMessage.value = "Не удалось загрузить курсы металлов"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadGoldRate()
    }
}