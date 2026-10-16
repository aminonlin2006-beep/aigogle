package com.example.data.converters

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * TypeConverters for Room to handle complex data types
 * such as java.util.Date, List<String>, List<Long>, and Map<String, String>.
 */
class AppTypeConverters {

    // --- Date <-> Long ---

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- List<String> <-> JSON String ---

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    // --- List<Long> <-> JSON String ---

    @TypeConverter
    fun fromLongList(list: List<Long>?): String {
        if (list == null || list.isEmpty()) return "[]"
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Long>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getLong(i))
            }
        } catch (_: Exception) {}
        return list
    }

    // --- Map<String, String> <-> JSON String ---

    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String {
        if (map == null || map.isEmpty()) return "{}"
        val jsonObject = JSONObject()
        map.forEach { (key, value) -> jsonObject.put(key, value) }
        return jsonObject.toString()
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        try {
            val jsonObject = JSONObject(value)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
        } catch (_: Exception) {}
        return map
    }
}
