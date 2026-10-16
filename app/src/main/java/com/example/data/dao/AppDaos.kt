package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE deleted = 0 ORDER BY active DESC, name ASC")
    fun getAllFarms(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE deleted = 0 AND active = 1 ORDER BY name ASC")
    fun getActiveFarms(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE id = :id AND deleted = 0")
    fun getFarmById(id: String): Flow<FarmEntity?>

    @Query("SELECT * FROM farms WHERE id = :id")
    suspend fun getFarmByIdSync(id: String): FarmEntity?

    @Query("SELECT * FROM farms")
    suspend fun getAllFarmsIncludingDeleted(): List<FarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(farm: FarmEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(farms: List<FarmEntity>)

    @Update
    suspend fun update(farm: FarmEntity)

    @Query("UPDATE farms SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteFarm(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM farms")
    suspend fun clearTable()
}

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots WHERE farmId = :farmId AND deleted = 0 ORDER BY active DESC, name ASC")
    fun getPlotsForFarm(farmId: String): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE farmId = :farmId AND isDefault = 0 AND deleted = 0 ORDER BY active DESC, name ASC")
    fun getRealPlotsForFarm(farmId: String): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE farmId = :farmId AND isDefault = 1 AND deleted = 0 LIMIT 1")
    suspend fun getDefaultPlotForFarm(farmId: String): PlotEntity?

    @Query("SELECT * FROM plots WHERE deleted = 0")
    fun getAllPlots(): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE deleted = 0")
    suspend fun getAllActivePlotsSync(): List<PlotEntity>

    @Query("SELECT * FROM plots WHERE farmId = :farmId AND deleted = 0")
    suspend fun getPlotsForFarmSync(farmId: String): List<PlotEntity>

    @Query("SELECT * FROM plots")
    suspend fun getAllPlotsIncludingDeleted(): List<PlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plot: PlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plots: List<PlotEntity>)

    @Update
    suspend fun update(plot: PlotEntity)

    @Query("UPDATE plots SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeletePlot(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE plots SET deleted = 1, updatedAt = :updatedAt WHERE farmId = :farmId")
    suspend fun softDeletePlotsByFarmId(farmId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM plots")
    suspend fun clearTable()
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE deleted = 0 ORDER BY active DESC, name COLLATE UNICODE ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id AND deleted = 0")
    fun getPersonById(id: String): Flow<PersonEntity?>

    @Query("SELECT * FROM people")
    suspend fun getAllPeopleIncludingDeleted(): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: PersonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(people: List<PersonEntity>)

    @Update
    suspend fun update(person: PersonEntity)

    @Query("UPDATE people SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeletePerson(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM people")
    suspend fun clearTable()
}

@Dao
interface RoleDao {
    @Query("SELECT * FROM roles WHERE deleted = 0 ORDER BY name ASC")
    fun getAllRoles(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles WHERE deleted = 0 AND hidden = 0 ORDER BY name ASC")
    fun getVisibleRoles(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles")
    suspend fun getAllRolesIncludingDeleted(): List<RoleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(role: RoleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(roles: List<RoleEntity>)

    @Update
    suspend fun update(role: RoleEntity)

    @Query("UPDATE roles SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteRole(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM roles")
    suspend fun clearTable()
}

@Dao
interface StageDao {
    @Query("SELECT * FROM stages WHERE deleted = 0 ORDER BY orderIndex ASC, name ASC")
    fun getAllStages(): Flow<List<StageEntity>>

    @Query("SELECT * FROM stages")
    suspend fun getAllStagesIncludingDeleted(): List<StageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stage: StageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<StageEntity>)

    @Update
    suspend fun update(stage: StageEntity)

    @Query("DELETE FROM stages")
    suspend fun clearTable()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE deleted = 0 ORDER BY createdAt ASC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE stageId = :stageId AND deleted = 0 ORDER BY createdAt ASC")
    fun getActivitiesForStage(stageId: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE stageId = :stageId AND deleted = 0")
    suspend fun getActivitiesForStageSync(stageId: String): List<ActivityEntity>

    @Query("SELECT * FROM activities")
    suspend fun getAllActivitiesIncludingDeleted(): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>)

    @Update
    suspend fun update(activity: ActivityEntity)

    @Query("DELETE FROM activities")
    suspend fun clearTable()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)

    @Query("SELECT * FROM device_settings WHERE id = 1 LIMIT 1")
    fun getDeviceSettings(): Flow<DeviceSettingsEntity?>

    @Query("SELECT * FROM device_settings WHERE id = 1 LIMIT 1")
    suspend fun getDeviceSettingsSync(): DeviceSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(settings: DeviceSettingsEntity)

    @Query("DELETE FROM settings")
    suspend fun clearTable()
}

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM snapshots ORDER BY createdAt DESC")
    fun getSnapshots(): Flow<List<SnapshotEntity>>

    @Query("SELECT * FROM snapshots WHERE kind = 'daily' ORDER BY createdAt DESC")
    suspend fun getDailySnapshotsSync(): List<SnapshotEntity>

    @Query("SELECT * FROM snapshots WHERE kind = 'beforeRestore' ORDER BY createdAt DESC")
    suspend fun getBeforeRestoreSnapshotsSync(): List<SnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: SnapshotEntity)

    @Query("DELETE FROM snapshots WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface MetaDao {
    @Query("SELECT * FROM meta WHERE id = 1 LIMIT 1")
    fun getMeta(): Flow<MetaEntity?>

    @Query("SELECT * FROM meta WHERE id = 1 LIMIT 1")
    suspend fun getMetaSync(): MetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(meta: MetaEntity)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE deleted = 0 ORDER BY name ASC")
    fun getAllTags(): Flow<List<com.example.data.entities.TagEntity>>

    @Query("SELECT * FROM tags WHERE (targetType = :targetType OR targetType = 'all') AND deleted = 0 ORDER BY name ASC")
    fun getTagsForTarget(targetType: String): Flow<List<com.example.data.entities.TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id AND deleted = 0")
    fun getTagById(id: String): Flow<com.example.data.entities.TagEntity?>

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsIncludingDeleted(): List<com.example.data.entities.TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: com.example.data.entities.TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<com.example.data.entities.TagEntity>)

    @Update
    suspend fun update(tag: com.example.data.entities.TagEntity)

    @Query("UPDATE tags SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteTag(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM tags")
    suspend fun clearTable()
}

