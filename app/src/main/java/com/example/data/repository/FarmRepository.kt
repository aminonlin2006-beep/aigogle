package com.example.data.repository

import com.example.data.dao.FarmDao
import com.example.data.entities.FarmEntity
import kotlinx.coroutines.flow.Flow

class FarmRepository(private val farmDao: FarmDao) {

    val allFarms: Flow<List<FarmEntity>> = farmDao.getAllFarms()

    val activeFarms: Flow<List<FarmEntity>> = farmDao.getActiveFarms()

    fun getFarmById(id: String): Flow<FarmEntity?> = farmDao.getFarmById(id)

    suspend fun getFarmByIdSync(id: String): FarmEntity? = farmDao.getFarmByIdSync(id)

    suspend fun insert(farm: FarmEntity) = farmDao.insert(farm)

    suspend fun update(farm: FarmEntity) = farmDao.update(farm)

    suspend fun delete(farmId: String) = farmDao.softDeleteFarm(farmId)
}
