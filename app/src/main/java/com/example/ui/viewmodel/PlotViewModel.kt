package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entities.PlotEntity
import com.example.data.repository.PlotRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlotViewModel(
    application: Application,
    private val repository: PlotRepository = PlotRepository(AppDatabase.getDatabase(application).plotDao())
) : AndroidViewModel(application) {

    val allPlots: StateFlow<List<PlotEntity>> = repository.allPlots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getPlotsForFarm(farmId: String): StateFlow<List<PlotEntity>> = repository.getPlotsForFarm(farmId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getRealPlotsForFarm(farmId: String): StateFlow<List<PlotEntity>> = repository.getRealPlotsForFarm(farmId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertPlot(plot: PlotEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insert(plot)
            onDone()
        }
    }

    fun updatePlot(plot: PlotEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(plot)
            onDone()
        }
    }

    fun deletePlot(plotId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(plotId)
            onDone()
        }
    }

    fun deletePlotsByFarmId(farmId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePlotsByFarmId(farmId)
            onDone()
        }
    }
}
