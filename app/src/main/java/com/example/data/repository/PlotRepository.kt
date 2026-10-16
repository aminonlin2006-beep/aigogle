package com.example.data.repository

import com.example.data.dao.PlotDao
import com.example.data.entities.PlotEntity
import kotlinx.coroutines.flow.Flow

class PlotRepository(private val plotDao: PlotDao) {

    val allPlots: Flow<List<PlotEntity>> = plotDao.getAllPlots()

    fun getPlotsForFarm(farmId: String): Flow<List<PlotEntity>> = plotDao.getPlotsForFarm(farmId)

    fun getRealPlotsForFarm(farmId: String): Flow<List<PlotEntity>> = plotDao.getRealPlotsForFarm(farmId)

    suspend fun getDefaultPlotForFarm(farmId: String): PlotEntity? = plotDao.getDefaultPlotForFarm(farmId)

    suspend fun insert(plot: PlotEntity) = plotDao.insert(plot)

    suspend fun update(plot: PlotEntity) = plotDao.update(plot)

    suspend fun delete(plotId: String) = plotDao.softDeletePlot(plotId)

    suspend fun deletePlotsByFarmId(farmId: String) = plotDao.softDeletePlotsByFarmId(farmId)
}
