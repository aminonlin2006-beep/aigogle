package com.example

import com.example.data.converters.AppTypeConverters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TypeConvertersTest {

    private val converter = AppTypeConverters()

    @Test
    fun testDateConversion() {
        val now = Date()
        val timestamp = converter.dateToTimestamp(now)
        assertNotNull(timestamp)
        assertEquals(now.time, timestamp)

        val restoredDate = converter.fromTimestamp(timestamp)
        assertNotNull(restoredDate)
        assertEquals(now, restoredDate)

        assertNull(converter.dateToTimestamp(null))
        assertNull(converter.fromTimestamp(null))
    }

    @Test
    fun testStringListConversion() {
        val list = listOf("role1", "role2", "role3")
        val json = converter.fromStringList(list)
        val restoredList = converter.toStringList(json)

        assertEquals(3, restoredList.size)
        assertEquals("role1", restoredList[0])
        assertEquals("role2", restoredList[1])
        assertEquals("role3", restoredList[2])

        assertEquals(emptyList<String>(), converter.toStringList(null))
        assertEquals(emptyList<String>(), converter.toStringList(""))
    }

    @Test
    fun testLongListConversion() {
        val list = listOf(100L, 200L, 300L)
        val json = converter.fromLongList(list)
        val restoredList = converter.toLongList(json)

        assertEquals(3, restoredList.size)
        assertEquals(100L, restoredList[0])
        assertEquals(200L, restoredList[1])
        assertEquals(300L, restoredList[2])
    }

    @Test
    fun testStringMapConversion() {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val json = converter.fromStringMap(map)
        val restoredMap = converter.toStringMap(json)

        assertEquals(2, restoredMap.size)
        assertEquals("value1", restoredMap["key1"])
        assertEquals("value2", restoredMap["key2"])
    }
}
