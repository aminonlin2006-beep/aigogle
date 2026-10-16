package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entities.TagEntity
import com.example.data.repository.TagRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagViewModel(
    application: Application,
    private val repository: TagRepository = TagRepository(AppDatabase.getDatabase(application).tagDao())
) : AndroidViewModel(application) {

    val allTags: StateFlow<List<TagEntity>> = repository.allTags
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getTagsForTarget(targetType: String): StateFlow<List<TagEntity>> = repository.getTagsForTarget(targetType)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertTag(tag: TagEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insert(tag)
            onDone()
        }
    }

    fun updateTag(tag: TagEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(tag)
            onDone()
        }
    }

    fun deleteTag(tagId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(tagId)
            onDone()
        }
    }
}
