package com.example.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

typealias Farm = FarmEntity
typealias Plot = PlotEntity
typealias Person = PersonEntity
typealias Role = RoleEntity
typealias Tag = TagEntity
typealias Task = TaskEntity

@Entity(
    tableName = "tags",
    indices = [
        Index("deleted"),
        Index("targetType"),
        Index("name")
    ]
)
data class TagEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long = 0xFF2E7D32,
    val targetType: String = "all", // "all", "farm", "plot"
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(
    tableName = "farms",
    indices = [
        Index("deleted"),
        Index("active")
    ]
)
data class FarmEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val areaM2: Double? = null,
    val ownership: String? = "owned", // "owned" = ملکی, "rented" = اجاره‌ای, "mixed" = ترکیبی
    val waterSource: String? = null, // "well" = چاه, "canal" = نوبت کانال, "both" = هر دو
    val note: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(
    tableName = "plots",
    indices = [
        Index("farmId"),
        Index("deleted"),
        Index("isDefault"),
        Index("active")
    ]
)
data class PlotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val name: String,
    val workType: String = "farming", // "farming" = زراعت, "nursery" = نهالستان
    val areaM2: Double? = null,
    val note: String = "",
    val active: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(
    tableName = "people",
    indices = [
        Index("deleted"),
        Index("active"),
        Index("name")
    ]
)
data class PersonEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String = "",
    val roleIdsJson: String = "[]",
    val note: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(
    tableName = "roles",
    indices = [
        Index("deleted"),
        Index("hidden")
    ]
)
data class RoleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val builtInKey: String? = null, // "worker", "foreman", "buyer", "seller"
    val hidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(
    tableName = "stages",
    indices = [
        Index("deleted"),
        Index("hidden"),
        Index("orderIndex")
    ]
)
data class StageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val builtInKey: String? = null, // "planting", "tending", "harvest", "other"
    val hidden: Boolean = false,
    val color: Long = 0xFF2E7D32,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(
    tableName = "activities",
    indices = [
        Index("stageId"),
        Index("deleted")
    ]
)
data class ActivityEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val stageId: String,
    val name: String,
    val color: Long = 0xFF43A047,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
)

@Entity(tableName = "settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val enabledWorkTypesJson: String = "[\"farming\",\"nursery\"]",
    val enabledModulesJson: String = "[\"tasks\"]", // Step 2: tasks module (Decision 159)
    val workMode: String = "local",
    val guideDismissed: Boolean = false
)

@Entity(
    tableName = "tasks",
    indices = [
        Index("deleted"),
        Index("date"),
        Index("status"),
        Index("priority"),
        Index("farmId")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val farmId: String? = null,
    val plotId: String? = null,
    val date: String, // Jalali date key e.g. "1405-07-24"
    val priority: String = "normal", // "urgent" (فوری), "important" (مهم), "normal" (عادی)
    val status: String = "pending", // "pending" (در انتظار), "completed" (انجام شد), "cancelled" (لغو شد)
    val assignedPersonIdsJson: String = "[]",
    val stageId: String? = null,
    val activityId: String? = null,
    val manDays: Double? = null, // حجم کار به نفرروز (Decision 55)
    val personCount: Int? = null, // تعداد نفر
    val note: String = "",
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "owner",
    val deleted: Boolean = false
) {
    val estimatedDays: Double?
        get() {
            if (manDays != null && personCount != null && personCount > 0) {
                return manDays / personCount
            }
            return null
        }
}

@Entity(tableName = "device_settings")
data class DeviceSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val fontSize: String = "normal", // "normal" | "large"
    val theme: String = "light", // "light" | "dark"
    val tileLayoutJson: String = "[\"farms\",\"people\",\"tags\"]",
    val hiddenTilesJson: String = "[]"
)

@Entity(
    tableName = "snapshots",
    indices = [
        Index("createdAt"),
        Index("kind")
    ]
)
data class SnapshotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val kind: String = "daily", // "daily" | "beforeRestore"
    val data: String = "{}"
)

@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey val id: Int = 1,
    val unsentChanges: Int = 0,
    val lastDailySnapshotDate: String = ""
)
