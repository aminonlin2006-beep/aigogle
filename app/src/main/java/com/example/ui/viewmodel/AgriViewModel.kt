package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entities.ActivityEntity
import com.example.data.entities.AppSettingsEntity
import com.example.data.entities.DeviceSettingsEntity
import com.example.data.entities.FarmEntity
import com.example.data.entities.MetaEntity
import com.example.data.entities.PersonEntity
import com.example.data.entities.PlotEntity
import com.example.data.entities.RoleEntity
import com.example.data.entities.SnapshotEntity
import com.example.data.entities.StageEntity
import com.example.data.entities.TagEntity
import com.example.data.entities.TaskEntity
import com.example.data.gateway.DataGateway
import com.example.util.PersianDateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

sealed class AppScreen {
    object Home : AppScreen()
    object Tasks : AppScreen() // Step 2: کارهای آینده
    object Farms : AppScreen()
    data class FarmDetail(val farmId: String) : AppScreen()
    object People : AppScreen()
    object Tags : AppScreen()
    object Settings : AppScreen()
    object Backup : AppScreen()
}

class AgriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val gateway = DataGateway(db)

    // Current Screen
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen navigation stack
    private val screenStack = mutableListOf<AppScreen>(AppScreen.Home)

    fun navigateTo(screen: AppScreen) {
        screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    // Flows
    val farms: StateFlow<List<FarmEntity>> = gateway.getFarmsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlots: StateFlow<List<PlotEntity>> = gateway.getAllPlotsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val people: StateFlow<List<PersonEntity>> = gateway.getPeopleFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val roles: StateFlow<List<RoleEntity>> = gateway.getRolesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stages: StateFlow<List<StageEntity>> = gateway.getStagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activities: StateFlow<List<ActivityEntity>> = gateway.getActivitiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettingsEntity?> = gateway.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val deviceSettings: StateFlow<DeviceSettingsEntity?> = gateway.getDeviceSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val meta: StateFlow<MetaEntity?> = gateway.getMetaFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val snapshots: StateFlow<List<SnapshotEntity>> = gateway.getSnapshotsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = gateway.getTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = gateway.getPendingTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<TagEntity>> = gateway.getTagsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    init {
        viewModelScope.launch {
            gateway.checkDailySnapshot()
        }
    }

    // --- Farms & Plots ---

    fun saveFarm(farm: FarmEntity, initialWorkType: String = "farming", onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.saveFarm(farm, initialWorkType)
            onDone()
        }
    }

    fun deleteFarm(farmId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deleteFarm(farmId)
            onDone()
        }
    }

    fun savePlot(plot: PlotEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.savePlot(plot)
            onDone()
        }
    }

    fun deletePlot(plotId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deletePlot(plotId)
            onDone()
        }
    }

    // --- People & Roles ---

    fun savePerson(person: PersonEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.savePerson(person)
            onDone()
        }
    }

    fun deletePerson(personId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deletePerson(personId)
            onDone()
        }
    }

    fun saveRole(role: RoleEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.saveRole(role)
            onDone()
        }
    }

    fun toggleRoleHidden(role: RoleEntity) {
        viewModelScope.launch {
            gateway.saveRole(role.copy(hidden = !role.hidden))
        }
    }

    fun deleteRole(roleId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deleteRole(roleId)
            onDone()
        }
    }

    // --- Stages & Activities ---

    fun saveStage(stage: StageEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.saveStage(stage)
            onDone()
        }
    }

    fun toggleStageHidden(stageId: String, hidden: Boolean) {
        viewModelScope.launch {
            gateway.toggleStageHidden(stageId, hidden)
        }
    }

    fun deleteStage(stageId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deleteStage(stageId)
            onDone()
        }
    }

    fun saveActivity(activity: ActivityEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.saveActivity(activity)
            onDone()
        }
    }

    fun deleteActivity(activityId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deleteActivity(activityId)
            onDone()
        }
    }

    fun mergeActivities(sourceId: String, targetId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.mergeActivities(sourceId, targetId)
            onDone()
        }
    }

    // --- Settings ---

    fun toggleWorkType(type: String) {
        val current = settings.value ?: return
        viewModelScope.launch {
            val enabledTypes = parseJsonStringList(current.enabledWorkTypesJson).toMutableList()
            if (enabledTypes.contains(type)) {
                // Trying to disable
                if (enabledTypes.size <= 1) {
                    _userMessage.value = "حداقل یک نوع کار باید روشن بماند."
                    return@launch
                }
                val (canDisable, blockingFarms) = gateway.canDisableWorkType(type)
                if (!canDisable) {
                    val farmList = blockingFarms.joinToString("، ")
                    _userMessage.value = "اول این مزرعه‌ها را غیرفعال کن: $farmList"
                    return@launch
                }
                enabledTypes.remove(type)
            } else {
                enabledTypes.add(type)
            }
            val json = JSONArray(enabledTypes).toString()
            gateway.saveSettings(current.copy(enabledWorkTypesJson = json))
        }
    }

    fun setFontSize(size: String) {
        val current = deviceSettings.value ?: DeviceSettingsEntity()
        viewModelScope.launch {
            gateway.saveDeviceSettings(current.copy(fontSize = size))
        }
    }

    fun setTheme(theme: String) {
        val current = deviceSettings.value ?: DeviceSettingsEntity()
        viewModelScope.launch {
            gateway.saveDeviceSettings(current.copy(theme = theme))
        }
    }

    fun updateTileLayout(order: List<String>, hidden: List<String>) {
        val current = deviceSettings.value ?: DeviceSettingsEntity()
        viewModelScope.launch {
            val orderJson = JSONArray(order).toString()
            val hiddenJson = JSONArray(hidden).toString()
            gateway.saveDeviceSettings(current.copy(tileLayoutJson = orderJson, hiddenTilesJson = hiddenJson))
        }
    }

    // --- Backup & Restore ---

    suspend fun getBackupJson(): String {
        return gateway.exportDatabaseToJson()
    }

    fun confirmBackupSent() {
        viewModelScope.launch {
            gateway.resetUnsentChanges()
            _userMessage.value = "پشتیبان با موفقیت ثبت شد."
        }
    }

    fun restoreBackup(jsonString: String, isInternalSnapshot: Boolean = false, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = gateway.restoreDatabaseFromJson(jsonString, isInternalSnapshot)
            if (result.isSuccess) {
                _userMessage.value = "اطلاعات با موفقیت بازگردانی شد."
                onComplete(true)
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "خطا در بازگردانی فایل"
                onComplete(false)
            }
        }
    }

    // --- Tasks (Step 2: کارهای آینده) ---

    fun saveTask(task: TaskEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.saveTask(task)
            onDone()
        }
    }

    fun markTaskCompleted(taskId: String) {
        viewModelScope.launch {
            gateway.updateTaskStatus(taskId, "completed")
            _userMessage.value = "کار با موفقیت انجام شد."
        }
    }

    fun markTaskCancelled(taskId: String) {
        viewModelScope.launch {
            gateway.updateTaskStatus(taskId, "cancelled")
            _userMessage.value = "کار لغو شد."
        }
    }

    fun rescheduleTask(taskId: String, newDate: String) {
        viewModelScope.launch {
            gateway.rescheduleTask(taskId, newDate)
            _userMessage.value = "تاریخ کار به‌روزرسانی شد."
        }
    }

    fun deleteTask(taskId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deleteTask(taskId)
            onDone()
        }
    }

    // --- Tags ---

    fun saveTag(tag: TagEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.saveTag(tag)
            onDone()
        }
    }

    fun deleteTag(tagId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            gateway.deleteTag(tagId)
            onDone()
        }
    }

    fun toggleModule(moduleKey: String) {
        val current = settings.value ?: return
        viewModelScope.launch {
            val enabledModules = parseJsonStringList(current.enabledModulesJson).toMutableList()
            if (enabledModules.contains(moduleKey)) {
                enabledModules.remove(moduleKey)
            } else {
                enabledModules.add(moduleKey)
            }
            gateway.saveSettings(current.copy(enabledModulesJson = JSONArray(enabledModules).toString()))
        }
    }

    // --- Helpers ---

    fun parseJsonStringList(json: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }
}
