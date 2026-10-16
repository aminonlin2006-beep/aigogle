package com.example.data.repository

import com.example.data.dao.TaskDao
import com.example.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)

    fun getOverdueTasks(todayDate: String): Flow<List<TaskEntity>> = taskDao.getOverdueTasks(todayDate)

    fun getTaskById(id: String): Flow<TaskEntity?> = taskDao.getTaskById(id)

    suspend fun insert(task: TaskEntity) = taskDao.insert(task)

    suspend fun update(task: TaskEntity) = taskDao.update(task)

    suspend fun markCompleted(taskId: String) =
        taskDao.updateStatus(taskId, "completed", System.currentTimeMillis())

    suspend fun markCancelled(taskId: String) =
        taskDao.updateStatus(taskId, "cancelled", null)

    suspend fun reschedule(taskId: String, newDate: String) =
        taskDao.rescheduleTask(taskId, newDate)

    suspend fun delete(taskId: String) = taskDao.softDeleteTask(taskId)
}
