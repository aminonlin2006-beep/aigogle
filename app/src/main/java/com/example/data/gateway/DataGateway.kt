package com.example.data.gateway

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
import com.example.util.PersianDateUtils
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class DataGateway(private val db: AppDatabase) {

    private val farmDao = db.farmDao()
    private val plotDao = db.plotDao()
    private val personDao = db.personDao()
    private val roleDao = db.roleDao()
    private val stageDao = db.stageDao()
    private val activityDao = db.activityDao()
    private val settingsDao = db.settingsDao()
    private val snapshotDao = db.snapshotDao()
    private val metaDao = db.metaDao()
    private val taskDao = db.taskDao()
    private val tagDao = db.tagDao()

    // ----------------------------------------------------
    // INITIALIZATION & DAILY SNAPSHOT
    // ----------------------------------------------------

    suspend fun checkDailySnapshot() {
        AppDatabase.seedDatabase(db)
        val todayKey = PersianDateUtils.today().toKey()
        val meta = metaDao.getMetaSync() ?: MetaEntity(id = 1)
        if (meta.lastDailySnapshotDate != todayKey) {
            // Take daily snapshot
            val currentDataJson = exportDatabaseToJson()
            val snapshot = SnapshotEntity(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                kind = "daily",
                data = currentDataJson
            )
            snapshotDao.insert(snapshot)

            // Keep only the latest 7 daily snapshots
            val dailyList = snapshotDao.getDailySnapshotsSync()
            if (dailyList.size > 7) {
                for (i in 7 until dailyList.size) {
                    snapshotDao.deleteById(dailyList[i].id)
                }
            }

            metaDao.insertOrUpdate(meta.copy(lastDailySnapshotDate = todayKey))
        }
    }

    private suspend fun incrementUnsentChanges() {
        val currentMeta = metaDao.getMetaSync() ?: MetaEntity(id = 1)
        metaDao.insertOrUpdate(currentMeta.copy(unsentChanges = currentMeta.unsentChanges + 1))
    }

    suspend fun resetUnsentChanges() {
        val currentMeta = metaDao.getMetaSync() ?: MetaEntity(id = 1)
        metaDao.insertOrUpdate(currentMeta.copy(unsentChanges = 0))
    }

    suspend fun setUnsentChanges(count: Int) {
        val currentMeta = metaDao.getMetaSync() ?: MetaEntity(id = 1)
        metaDao.insertOrUpdate(currentMeta.copy(unsentChanges = count))
    }

    // ----------------------------------------------------
    // FARMS & PLOTS
    // ----------------------------------------------------

    fun getFarmsFlow(): Flow<List<FarmEntity>> = farmDao.getAllFarms()

    fun getActiveFarmsFlow(): Flow<List<FarmEntity>> = farmDao.getActiveFarms()

    fun getFarmByIdFlow(id: String): Flow<FarmEntity?> = farmDao.getFarmById(id)

    suspend fun getFarmById(id: String): FarmEntity? = farmDao.getFarmByIdSync(id)

    fun getPlotsForFarmFlow(farmId: String): Flow<List<PlotEntity>> = plotDao.getPlotsForFarm(farmId)

    fun getAllPlotsFlow(): Flow<List<PlotEntity>> = plotDao.getAllPlots()

    suspend fun getRealPlotsForFarm(farmId: String): List<PlotEntity> {
        return plotDao.getPlotsForFarmSync(farmId).filter { !it.isDefault }
    }

    suspend fun saveFarm(
        farm: FarmEntity,
        initialWorkType: String = "farming"
    ): String {
        val now = System.currentTimeMillis()
        val isNew = farmDao.getFarmByIdSync(farm.id) == null

        if (isNew) {
            farmDao.insert(farm.copy(createdAt = now, updatedAt = now))
            // Create hidden default plot for the farm
            val defaultPlot = PlotEntity(
                id = UUID.randomUUID().toString(),
                farmId = farm.id,
                name = "",
                workType = initialWorkType,
                areaM2 = farm.areaM2,
                note = "",
                active = true,
                isDefault = true,
                createdAt = now,
                updatedAt = now
            )
            plotDao.insert(defaultPlot)
        } else {
            farmDao.update(farm.copy(updatedAt = now))
            // Also update default plot's area or active state if appropriate
            val plots = plotDao.getPlotsForFarmSync(farm.id)
            val defaultPlot = plots.find { it.isDefault }
            if (defaultPlot != null) {
                plotDao.update(defaultPlot.copy(active = farm.active, updatedAt = now))
            }
        }
        incrementUnsentChanges()
        checkGuideDismissal()
        return farm.id
    }

    suspend fun deleteFarm(farmId: String) {
        val now = System.currentTimeMillis()
        val farm = farmDao.getFarmByIdSync(farmId) ?: return
        farmDao.update(farm.copy(deleted = true, updatedAt = now))

        // Soft delete all plots belonging to this farm
        val plots = plotDao.getPlotsForFarmSync(farmId)
        for (plot in plots) {
            plotDao.update(plot.copy(deleted = true, updatedAt = now))
        }
        incrementUnsentChanges()
    }

    suspend fun savePlot(plot: PlotEntity): String {
        val now = System.currentTimeMillis()
        val existing = plotDao.getAllActivePlotsSync().find { it.id == plot.id }
        if (existing == null) {
            plotDao.insert(plot.copy(createdAt = now, updatedAt = now))
        } else {
            plotDao.update(plot.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return plot.id
    }

    suspend fun deletePlot(plotId: String) {
        val now = System.currentTimeMillis()
        val plots = plotDao.getAllActivePlotsSync()
        val plot = plots.find { it.id == plotId } ?: return
        plotDao.update(plot.copy(deleted = true, updatedAt = now))
        incrementUnsentChanges()
    }

    /**
     * Determines work type display label for a farm card:
     * - If active real plots exist: reads from active real plots.
     * - If no real plots: reads from default plot.
     * - If contains both farming & nursery: "زراعت و نهالستان".
     */
    suspend fun getFarmWorkTypeLabel(farmId: String): String {
        val plots = plotDao.getPlotsForFarmSync(farmId).filter { it.active }
        val realPlots = plots.filter { !it.isDefault }

        val activeWorkTypes = if (realPlots.isNotEmpty()) {
            realPlots.map { it.workType }.toSet()
        } else {
            val defaultPlot = plots.find { it.isDefault }
            if (defaultPlot != null) setOf(defaultPlot.workType) else setOf("farming")
        }

        val hasFarming = activeWorkTypes.contains("farming")
        val hasNursery = activeWorkTypes.contains("nursery")

        return when {
            hasFarming && hasNursery -> "زراعت و نهالستان"
            hasNursery -> "نهالستان"
            else -> "زراعت"
        }
    }

    // ----------------------------------------------------
    // PEOPLE & ROLES
    // ----------------------------------------------------

    fun getPeopleFlow(): Flow<List<PersonEntity>> = personDao.getAllPeople()

    fun getRolesFlow(): Flow<List<RoleEntity>> = roleDao.getAllRoles()

    suspend fun savePerson(person: PersonEntity): String {
        val now = System.currentTimeMillis()
        val isNew = personDao.getAllPeopleIncludingDeleted().none { it.id == person.id }
        if (isNew) {
            personDao.insert(person.copy(createdAt = now, updatedAt = now))
        } else {
            personDao.update(person.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return person.id
    }

    suspend fun deletePerson(personId: String) {
        val now = System.currentTimeMillis()
        val person = personDao.getAllPeopleIncludingDeleted().find { it.id == personId } ?: return
        personDao.update(person.copy(deleted = true, updatedAt = now))
        incrementUnsentChanges()
    }

    suspend fun saveRole(role: RoleEntity): String {
        val now = System.currentTimeMillis()
        val isNew = roleDao.getAllRolesIncludingDeleted().none { it.id == role.id }
        if (isNew) {
            roleDao.insert(role.copy(createdAt = now, updatedAt = now))
        } else {
            roleDao.update(role.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return role.id
    }

    suspend fun deleteRole(roleId: String) {
        val now = System.currentTimeMillis()
        val role = roleDao.getAllRolesIncludingDeleted().find { it.id == roleId } ?: return
        if (role.builtInKey != null) {
            // Built-in roles cannot be deleted, only hidden
            return
        }
        roleDao.update(role.copy(deleted = true, updatedAt = now))
        incrementUnsentChanges()
    }

    // ----------------------------------------------------
    // STAGES & ACTIVITIES
    // ----------------------------------------------------

    fun getStagesFlow(): Flow<List<StageEntity>> = stageDao.getAllStages()

    fun getActivitiesFlow(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()

    fun getActivitiesForStageFlow(stageId: String): Flow<List<ActivityEntity>> =
        activityDao.getActivitiesForStage(stageId)

    suspend fun saveStage(stage: StageEntity): String {
        val now = System.currentTimeMillis()
        val isNew = stageDao.getAllStagesIncludingDeleted().none { it.id == stage.id }
        if (isNew) {
            stageDao.insert(stage.copy(createdAt = now, updatedAt = now))
        } else {
            stageDao.update(stage.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return stage.id
    }

    suspend fun toggleStageHidden(stageId: String, hidden: Boolean) {
        val now = System.currentTimeMillis()
        val stage = stageDao.getAllStagesIncludingDeleted().find { it.id == stageId } ?: return
        stageDao.update(stage.copy(hidden = hidden, updatedAt = now))
        incrementUnsentChanges()
    }

    suspend fun deleteStage(stageId: String) {
        val now = System.currentTimeMillis()
        val stage = stageDao.getAllStagesIncludingDeleted().find { it.id == stageId } ?: return
        if (stage.builtInKey != null) {
            // Built-in stages cannot be deleted
            return
        }
        stageDao.update(stage.copy(deleted = true, updatedAt = now))
        // Delete all its activities
        val activities = activityDao.getActivitiesForStageSync(stageId)
        for (act in activities) {
            activityDao.update(act.copy(deleted = true, updatedAt = now))
        }
        incrementUnsentChanges()
    }

    suspend fun saveActivity(activity: ActivityEntity): String {
        val now = System.currentTimeMillis()
        val isNew = activityDao.getAllActivitiesIncludingDeleted().none { it.id == activity.id }
        if (isNew) {
            activityDao.insert(activity.copy(createdAt = now, updatedAt = now))
        } else {
            activityDao.update(activity.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return activity.id
    }

    suspend fun deleteActivity(activityId: String) {
        val now = System.currentTimeMillis()
        val act = activityDao.getAllActivitiesIncludingDeleted().find { it.id == activityId } ?: return
        activityDao.update(act.copy(deleted = true, updatedAt = now))
        incrementUnsentChanges()
    }

    /**
     * Merge activity sourceId into targetId (Decision 124, 164)
     * All future records (from step 3 onwards) will point to targetId.
     * Source activity is marked deleted = true.
     */
    suspend fun mergeActivities(sourceId: String, targetId: String) {
        if (sourceId == targetId) return
        val now = System.currentTimeMillis()
        val source = activityDao.getAllActivitiesIncludingDeleted().find { it.id == sourceId } ?: return
        activityDao.update(source.copy(deleted = true, updatedAt = now))
        incrementUnsentChanges()
    }

    // ----------------------------------------------------
    // SETTINGS & DEVICE SETTINGS
    // ----------------------------------------------------

    fun getSettingsFlow(): Flow<AppSettingsEntity?> = settingsDao.getSettings()

    fun getDeviceSettingsFlow(): Flow<DeviceSettingsEntity?> = settingsDao.getDeviceSettings()

    fun getMetaFlow(): Flow<MetaEntity?> = metaDao.getMeta()

    fun getSnapshotsFlow(): Flow<List<SnapshotEntity>> = snapshotDao.getSnapshots()

    suspend fun saveSettings(settings: AppSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
        incrementUnsentChanges()
    }

    suspend fun saveDeviceSettings(settings: DeviceSettingsEntity) {
        // Device settings are local only and do NOT increment unsent changes
        settingsDao.insertOrUpdateDevice(settings)
    }

    /**
     * Checks if a work type can be disabled.
     * Returns Pair(allowed: Boolean, blockingFarmNames: List<String>)
     */
    suspend fun canDisableWorkType(workType: String): Pair<Boolean, List<String>> {
        val activeFarms = farmDao.getAllFarmsIncludingDeleted().filter { it.active && !it.deleted }
        val blockingFarms = mutableListOf<String>()

        for (farm in activeFarms) {
            val plots = plotDao.getPlotsForFarmSync(farm.id).filter { it.active && !it.deleted }
            val realPlots = plots.filter { !it.isDefault }
            val hasType = if (realPlots.isNotEmpty()) {
                realPlots.any { it.workType == workType }
            } else {
                plots.find { it.isDefault }?.workType == workType
            }
            if (hasType) {
                blockingFarms.add(farm.name)
            }
        }

        return Pair(blockingFarms.isEmpty(), blockingFarms)
    }

    private suspend fun checkGuideDismissal() {
        val settings = settingsDao.getSettingsSync() ?: return
        if (!settings.guideDismissed) {
            val farmCount = farmDao.getAllFarmsIncludingDeleted().count { !it.deleted }
            if (farmCount > 0) {
                settingsDao.insertOrUpdate(settings.copy(guideDismissed = true))
            }
        }
    }

    // ----------------------------------------------------
    // TASKS (STEP 2: کارهای آینده)
    // ----------------------------------------------------

    fun getTasksFlow(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksForDateFlow(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)

    fun getPendingTasksFlow(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    fun getOverdueTasksFlow(todayDate: String): Flow<List<TaskEntity>> = taskDao.getOverdueTasks(todayDate)

    fun getTaskByIdFlow(id: String): Flow<TaskEntity?> = taskDao.getTaskById(id)

    suspend fun saveTask(task: TaskEntity): String {
        val now = System.currentTimeMillis()
        val isNew = taskDao.getAllTasksIncludingDeleted().none { it.id == task.id }
        if (isNew) {
            taskDao.insert(task.copy(createdAt = now, updatedAt = now))
        } else {
            taskDao.update(task.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return task.id
    }

    suspend fun updateTaskStatus(taskId: String, status: String) {
        val completedAt = if (status == "completed") System.currentTimeMillis() else null
        taskDao.updateStatus(taskId, status, completedAt)
        incrementUnsentChanges()
    }

    suspend fun rescheduleTask(taskId: String, newDate: String) {
        taskDao.rescheduleTask(taskId, newDate)
        incrementUnsentChanges()
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.softDeleteTask(taskId)
        incrementUnsentChanges()
    }

    // ----------------------------------------------------
    // TAGS
    // ----------------------------------------------------

    fun getTagsFlow(): Flow<List<TagEntity>> = tagDao.getAllTags()

    fun getTagsForTargetFlow(targetType: String): Flow<List<TagEntity>> = tagDao.getTagsForTarget(targetType)

    fun getTagByIdFlow(id: String): Flow<TagEntity?> = tagDao.getTagById(id)

    suspend fun saveTag(tag: TagEntity): String {
        val now = System.currentTimeMillis()
        val existing = tagDao.getAllTagsIncludingDeleted().find { it.id == tag.id }
        if (existing == null) {
            tagDao.insert(tag.copy(createdAt = now, updatedAt = now))
        } else {
            tagDao.update(tag.copy(updatedAt = now))
        }
        incrementUnsentChanges()
        return tag.id
    }

    suspend fun deleteTag(tagId: String) {
        tagDao.softDeleteTag(tagId)
        incrementUnsentChanges()
    }

    // ----------------------------------------------------
    // BACKUP & RESTORE
    // ----------------------------------------------------

    suspend fun exportDatabaseToJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "agri_manager")
        root.put("exportedAt", System.currentTimeMillis())

        // Farms
        val farms = farmDao.getAllFarmsIncludingDeleted()
        val farmsArray = JSONArray()
        for (f in farms) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                f.areaM2?.let { put("areaM2", it) }
                f.ownership?.let { put("ownership", it) }
                f.waterSource?.let { put("waterSource", it) }
                put("note", f.note)
                put("active", f.active)
                put("createdAt", f.createdAt)
                put("updatedAt", f.updatedAt)
                put("createdBy", f.createdBy)
                put("deleted", f.deleted)
            }
            farmsArray.put(obj)
        }
        root.put("farms", farmsArray)

        // Plots
        val plots = plotDao.getAllPlotsIncludingDeleted()
        val plotsArray = JSONArray()
        for (p in plots) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("farmId", p.farmId)
                put("name", p.name)
                put("workType", p.workType)
                p.areaM2?.let { put("areaM2", it) }
                put("note", p.note)
                put("active", p.active)
                put("isDefault", p.isDefault)
                put("createdAt", p.createdAt)
                put("updatedAt", p.updatedAt)
                put("createdBy", p.createdBy)
                put("deleted", p.deleted)
            }
            plotsArray.put(obj)
        }
        root.put("plots", plotsArray)

        // People
        val people = personDao.getAllPeopleIncludingDeleted()
        val peopleArray = JSONArray()
        for (pe in people) {
            val obj = JSONObject().apply {
                put("id", pe.id)
                put("name", pe.name)
                put("phone", pe.phone)
                put("roleIdsJson", pe.roleIdsJson)
                put("note", pe.note)
                put("active", pe.active)
                put("createdAt", pe.createdAt)
                put("updatedAt", pe.updatedAt)
                put("createdBy", pe.createdBy)
                put("deleted", pe.deleted)
            }
            peopleArray.put(obj)
        }
        root.put("people", peopleArray)

        // Roles
        val roles = roleDao.getAllRolesIncludingDeleted()
        val rolesArray = JSONArray()
        for (r in roles) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                r.builtInKey?.let { put("builtInKey", it) }
                put("hidden", r.hidden)
                put("createdAt", r.createdAt)
                put("updatedAt", r.updatedAt)
                put("createdBy", r.createdBy)
                put("deleted", r.deleted)
            }
            rolesArray.put(obj)
        }
        root.put("roles", rolesArray)

        // Stages
        val stages = stageDao.getAllStagesIncludingDeleted()
        val stagesArray = JSONArray()
        for (s in stages) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                s.builtInKey?.let { put("builtInKey", it) }
                put("hidden", s.hidden)
                put("color", s.color)
                put("orderIndex", s.orderIndex)
                put("createdAt", s.createdAt)
                put("updatedAt", s.updatedAt)
                put("createdBy", s.createdBy)
                put("deleted", s.deleted)
            }
            stagesArray.put(obj)
        }
        root.put("stages", stagesArray)

        // Activities
        val activities = activityDao.getAllActivitiesIncludingDeleted()
        val activitiesArray = JSONArray()
        for (a in activities) {
            val obj = JSONObject().apply {
                put("id", a.id)
                put("stageId", a.stageId)
                put("name", a.name)
                put("color", a.color)
                put("createdAt", a.createdAt)
                put("updatedAt", a.updatedAt)
                put("createdBy", a.createdBy)
                put("deleted", a.deleted)
            }
            activitiesArray.put(obj)
        }
        root.put("activities", activitiesArray)

        // Tasks (Step 2: کارهای آینده)
        val tasks = taskDao.getAllTasksIncludingDeleted()
        val tasksArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                t.farmId?.let { put("farmId", it) }
                t.plotId?.let { put("plotId", it) }
                put("date", t.date)
                put("priority", t.priority)
                put("status", t.status)
                put("assignedPersonIdsJson", t.assignedPersonIdsJson)
                t.stageId?.let { put("stageId", it) }
                t.activityId?.let { put("activityId", it) }
                t.manDays?.let { put("manDays", it) }
                t.personCount?.let { put("personCount", it) }
                put("note", t.note)
                t.completedAt?.let { put("completedAt", it) }
                put("createdAt", t.createdAt)
                put("updatedAt", t.updatedAt)
                put("createdBy", t.createdBy)
                put("deleted", t.deleted)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Tags
        val tags = tagDao.getAllTagsIncludingDeleted()
        val tagsArray = JSONArray()
        for (t in tags) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("color", t.color)
                put("targetType", t.targetType)
                put("note", t.note)
                put("createdAt", t.createdAt)
                put("updatedAt", t.updatedAt)
                put("createdBy", t.createdBy)
                put("deleted", t.deleted)
            }
            tagsArray.put(obj)
        }
        root.put("tags", tagsArray)

        // Settings
        val settings = settingsDao.getSettingsSync() ?: AppSettingsEntity()
        val settingsObj = JSONObject().apply {
            put("enabledWorkTypesJson", settings.enabledWorkTypesJson)
            put("enabledModulesJson", settings.enabledModulesJson)
            put("workMode", settings.workMode)
            put("guideDismissed", settings.guideDismissed)
        }
        root.put("settings", settingsObj)

        return root.toString(2)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String, isInternalSnapshot: Boolean = false): Result<Unit> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("farms") || !root.has("plots") || !root.has("people") || !root.has("roles")) {
                return Result.failure(IllegalArgumentException("فایل نامعتبر است یا ساختار داده‌های اپ را ندارد."))
            }

            // 1. Create automatic "beforeRestore" snapshot
            val currentBackup = exportDatabaseToJson()
            val beforeRestoreSnapshot = SnapshotEntity(
                id = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                kind = "beforeRestore",
                data = currentBackup
            )
            // Remove previous beforeRestore snapshot if exists (Decision پ12: only keep the last beforeRestore snapshot)
            val oldBeforeRestores = snapshotDao.getBeforeRestoreSnapshotsSync()
            for (old in oldBeforeRestores) {
                snapshotDao.deleteById(old.id)
            }
            snapshotDao.insert(beforeRestoreSnapshot)

            // 2. Parse new data
            val newFarms = mutableListOf<FarmEntity>()
            val farmsArr = root.getJSONArray("farms")
            for (i in 0 until farmsArr.length()) {
                val o = farmsArr.getJSONObject(i)
                newFarms.add(
                    FarmEntity(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        areaM2 = if (o.has("areaM2") && !o.isNull("areaM2")) o.getDouble("areaM2") else null,
                        ownership = if (o.has("ownership") && !o.isNull("ownership")) o.getString("ownership") else null,
                        waterSource = if (o.has("waterSource") && !o.isNull("waterSource")) o.getString("waterSource") else null,
                        note = o.optString("note", ""),
                        active = o.optBoolean("active", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                        createdBy = o.optString("createdBy", "owner"),
                        deleted = o.optBoolean("deleted", false)
                    )
                )
            }

            val newPlots = mutableListOf<PlotEntity>()
            val plotsArr = root.getJSONArray("plots")
            for (i in 0 until plotsArr.length()) {
                val o = plotsArr.getJSONObject(i)
                newPlots.add(
                    PlotEntity(
                        id = o.getString("id"),
                        farmId = o.getString("farmId"),
                        name = o.optString("name", ""),
                        workType = o.optString("workType", "farming"),
                        areaM2 = if (o.has("areaM2") && !o.isNull("areaM2")) o.getDouble("areaM2") else null,
                        note = o.optString("note", ""),
                        active = o.optBoolean("active", true),
                        isDefault = o.optBoolean("isDefault", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                        createdBy = o.optString("createdBy", "owner"),
                        deleted = o.optBoolean("deleted", false)
                    )
                )
            }

            val newPeople = mutableListOf<PersonEntity>()
            val peopleArr = root.getJSONArray("people")
            for (i in 0 until peopleArr.length()) {
                val o = peopleArr.getJSONObject(i)
                newPeople.add(
                    PersonEntity(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        phone = o.optString("phone", ""),
                        roleIdsJson = o.optString("roleIdsJson", "[]"),
                        note = o.optString("note", ""),
                        active = o.optBoolean("active", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                        createdBy = o.optString("createdBy", "owner"),
                        deleted = o.optBoolean("deleted", false)
                    )
                )
            }

            val newRoles = mutableListOf<RoleEntity>()
            val rolesArr = root.getJSONArray("roles")
            for (i in 0 until rolesArr.length()) {
                val o = rolesArr.getJSONObject(i)
                newRoles.add(
                    RoleEntity(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        builtInKey = if (o.has("builtInKey") && !o.isNull("builtInKey")) o.getString("builtInKey") else null,
                        hidden = o.optBoolean("hidden", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                        createdBy = o.optString("createdBy", "owner"),
                        deleted = o.optBoolean("deleted", false)
                    )
                )
            }

            val newStages = mutableListOf<StageEntity>()
            if (root.has("stages")) {
                val stagesArr = root.getJSONArray("stages")
                for (i in 0 until stagesArr.length()) {
                    val o = stagesArr.getJSONObject(i)
                    newStages.add(
                        StageEntity(
                            id = o.getString("id"),
                            name = o.getString("name"),
                            builtInKey = if (o.has("builtInKey") && !o.isNull("builtInKey")) o.getString("builtInKey") else null,
                            hidden = o.optBoolean("hidden", false),
                            color = o.optLong("color", 0xFF2E7D32),
                            orderIndex = o.optInt("orderIndex", 0),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                            createdBy = o.optString("createdBy", "owner"),
                            deleted = o.optBoolean("deleted", false)
                        )
                    )
                }
            }

            val newActivities = mutableListOf<ActivityEntity>()
            if (root.has("activities")) {
                val actArr = root.getJSONArray("activities")
                for (i in 0 until actArr.length()) {
                    val o = actArr.getJSONObject(i)
                    newActivities.add(
                        ActivityEntity(
                            id = o.getString("id"),
                            stageId = o.getString("stageId"),
                            name = o.getString("name"),
                            color = o.optLong("color", 0xFF43A047),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                            createdBy = o.optString("createdBy", "owner"),
                            deleted = o.optBoolean("deleted", false)
                        )
                    )
                }
            }

            val newTasks = mutableListOf<TaskEntity>()
            if (root.has("tasks")) {
                val taskArr = root.getJSONArray("tasks")
                for (i in 0 until taskArr.length()) {
                    val o = taskArr.getJSONObject(i)
                    newTasks.add(
                        TaskEntity(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            farmId = if (o.has("farmId") && !o.isNull("farmId")) o.getString("farmId") else null,
                            plotId = if (o.has("plotId") && !o.isNull("plotId")) o.getString("plotId") else null,
                            date = o.optString("date", PersianDateUtils.today().toKey()),
                            priority = o.optString("priority", "normal"),
                            status = o.optString("status", "pending"),
                            assignedPersonIdsJson = o.optString("assignedPersonIdsJson", "[]"),
                            stageId = if (o.has("stageId") && !o.isNull("stageId")) o.getString("stageId") else null,
                            activityId = if (o.has("activityId") && !o.isNull("activityId")) o.getString("activityId") else null,
                            manDays = if (o.has("manDays") && !o.isNull("manDays")) o.getDouble("manDays") else null,
                            personCount = if (o.has("personCount") && !o.isNull("personCount")) o.getInt("personCount") else null,
                            note = o.optString("note", ""),
                            completedAt = if (o.has("completedAt") && !o.isNull("completedAt")) o.getLong("completedAt") else null,
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                            createdBy = o.optString("createdBy", "owner"),
                            deleted = o.optBoolean("deleted", false)
                        )
                    )
                }
            }

            val newTags = mutableListOf<TagEntity>()
            if (root.has("tags")) {
                val tagArr = root.getJSONArray("tags")
                for (i in 0 until tagArr.length()) {
                    val o = tagArr.getJSONObject(i)
                    newTags.add(
                        TagEntity(
                            id = o.getString("id"),
                            name = o.getString("name"),
                            color = if (o.has("color") && !o.isNull("color")) o.optLong("color", 0xFF2E7D32) else 0xFF2E7D32,
                            targetType = o.optString("targetType", "all"),
                            note = o.optString("note", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                            createdBy = o.optString("createdBy", "owner"),
                            deleted = o.optBoolean("deleted", false)
                        )
                    )
                }
            }

            var newSettings = AppSettingsEntity()
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                newSettings = AppSettingsEntity(
                    id = 1,
                    enabledWorkTypesJson = s.optString("enabledWorkTypesJson", "[\"farming\",\"nursery\"]"),
                    enabledModulesJson = s.optString("enabledModulesJson", "[\"tasks\"]"),
                    workMode = s.optString("workMode", "local"),
                    guideDismissed = s.optBoolean("guideDismissed", true)
                )
            }

            // 3. Clear and replace tables
            farmDao.clearTable()
            farmDao.insertAll(newFarms)

            plotDao.clearTable()
            plotDao.insertAll(newPlots)

            personDao.clearTable()
            personDao.insertAll(newPeople)

            roleDao.clearTable()
            roleDao.insertAll(newRoles)

            stageDao.clearTable()
            stageDao.insertAll(newStages)

            activityDao.clearTable()
            activityDao.insertAll(newActivities)

            taskDao.clearTable()
            taskDao.insertAll(newTasks)

            tagDao.clearTable()
            tagDao.insertAll(newTags)

            settingsDao.clearTable()
            settingsDao.insertOrUpdate(newSettings)

            // 4. Update counter:
            // Decision پ11: After restoring from file, counter is 0. After restoring from internal snapshot, counter is 1.
            val targetCounter = if (isInternalSnapshot) 1 else 0
            setUnsentChanges(targetCounter)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
