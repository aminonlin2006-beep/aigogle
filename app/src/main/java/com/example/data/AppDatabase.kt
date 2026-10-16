package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ActivityDao
import com.example.data.dao.FarmDao
import com.example.data.dao.MetaDao
import com.example.data.dao.PersonDao
import com.example.data.dao.PlotDao
import com.example.data.dao.RoleDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.SnapshotDao
import com.example.data.dao.StageDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.dao.TagDao
import com.example.data.entities.TagEntity
import java.util.UUID

@Database(
    entities = [
        FarmEntity::class,
        PlotEntity::class,
        PersonEntity::class,
        RoleEntity::class,
        StageEntity::class,
        ActivityEntity::class,
        AppSettingsEntity::class,
        DeviceSettingsEntity::class,
        SnapshotEntity::class,
        MetaEntity::class,
        TagEntity::class
    ],
    version = 2,
    exportSchema = false
)
@androidx.room.TypeConverters(com.example.data.converters.AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmDao(): FarmDao
    abstract fun plotDao(): PlotDao
    abstract fun personDao(): PersonDao
    abstract fun roleDao(): RoleDao
    abstract fun stageDao(): StageDao
    abstract fun activityDao(): ActivityDao
    abstract fun settingsDao(): SettingsDao
    abstract fun snapshotDao(): SnapshotDao
    abstract fun metaDao(): MetaDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agri_manager.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            val now = System.currentTimeMillis()

            // Built-in Roles: worker, foreman, buyer, seller
            if (database.roleDao().getAllRolesIncludingDeleted().isEmpty()) {
                val roles = listOf(
                    RoleEntity(id = UUID.randomUUID().toString(), name = "کارگر", builtInKey = "worker", createdAt = now, updatedAt = now),
                    RoleEntity(id = UUID.randomUUID().toString(), name = "سرکارگر", builtInKey = "foreman", createdAt = now, updatedAt = now),
                    RoleEntity(id = UUID.randomUUID().toString(), name = "خریدار", builtInKey = "buyer", createdAt = now, updatedAt = now),
                    RoleEntity(id = UUID.randomUUID().toString(), name = "فروشنده", builtInKey = "seller", createdAt = now, updatedAt = now)
                )
                database.roleDao().insertAll(roles)
            }

            // Built-in Stages: planting, tending, harvest, other
            if (database.stageDao().getAllStagesIncludingDeleted().isEmpty()) {
                val stages = listOf(
                    StageEntity(id = UUID.randomUUID().toString(), name = "کاشت", builtInKey = "planting", color = 0xFF2E7D32, orderIndex = 1, createdAt = now, updatedAt = now),
                    StageEntity(id = UUID.randomUUID().toString(), name = "داشت", builtInKey = "tending", color = 0xFF1565C0, orderIndex = 2, createdAt = now, updatedAt = now),
                    StageEntity(id = UUID.randomUUID().toString(), name = "برداشت", builtInKey = "harvest", color = 0xFFE65100, orderIndex = 3, createdAt = now, updatedAt = now),
                    StageEntity(id = UUID.randomUUID().toString(), name = "سایر", builtInKey = "other", color = 0xFF546E7A, orderIndex = 4, createdAt = now, updatedAt = now)
                )
                database.stageDao().insertAll(stages)
            }

            // Settings
            if (database.settingsDao().getSettingsSync() == null) {
                database.settingsDao().insertOrUpdate(AppSettingsEntity(id = 1))
            }
            if (database.settingsDao().getDeviceSettingsSync() == null) {
                database.settingsDao().insertOrUpdateDevice(DeviceSettingsEntity(id = 1))
            }
            // Meta
            if (database.metaDao().getMetaSync() == null) {
                database.metaDao().insertOrUpdate(MetaEntity(id = 1, unsentChanges = 0, lastDailySnapshotDate = ""))
            }
        }
    }
}
