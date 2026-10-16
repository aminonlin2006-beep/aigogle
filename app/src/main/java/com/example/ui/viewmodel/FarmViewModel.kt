package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entities.FarmEntity
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FarmViewModel(
    application: Application,
    private val repository: FarmRepository = FarmRepository(AppDatabase.getDatabase(application).farmDao())
) : AndroidViewModel(application) {

    val farms: StateFlow<List<FarmEntity>> = repository.allFarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeFarms: StateFlow<List<FarmEntity>> = repository.activeFarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getFarmById(farmId: String): StateFlow<FarmEntity?> = repository.getFarmById(farmId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun insertFarm(farm: FarmEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insert(farm)
            onDone()
        }
    }

    fun updateFarm(farm: FarmEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(farm)
            onDone()
        }
    }

    fun deleteFarm(farmId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(farmId)
            onDone()
        }
    }
}
