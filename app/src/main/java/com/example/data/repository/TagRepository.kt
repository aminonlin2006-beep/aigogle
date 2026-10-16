package com.example.data.repository

import com.example.data.dao.TagDao
import com.example.data.entities.TagEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {

    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()

    fun getTagsForTarget(targetType: String): Flow<List<TagEntity>> = tagDao.getTagsForTarget(targetType)

    fun getTagById(id: String): Flow<TagEntity?> = tagDao.getTagById(id)

    suspend fun insert(tag: TagEntity) = tagDao.insert(tag)

    suspend fun insertAll(tags: List<TagEntity>) = tagDao.insertAll(tags)

    suspend fun update(tag: TagEntity) = tagDao.update(tag)

    suspend fun delete(tagId: String) = tagDao.softDeleteTag(tagId)
}
