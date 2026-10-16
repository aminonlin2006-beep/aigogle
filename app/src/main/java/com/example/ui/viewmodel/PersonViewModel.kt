package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entities.PersonEntity
import com.example.data.entities.RoleEntity
import com.example.data.repository.PersonRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PersonViewModel(
    application: Application,
    private val repository: PersonRepository = PersonRepository(
        personDao = AppDatabase.getDatabase(application).personDao(),
        roleDao = AppDatabase.getDatabase(application).roleDao()
    )
) : AndroidViewModel(application) {

    val people: StateFlow<List<PersonEntity>> = repository.allPeople
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val roles: StateFlow<List<RoleEntity>> = repository.allRoles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visibleRoles: StateFlow<List<RoleEntity>> = repository.visibleRoles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getPersonById(personId: String): StateFlow<PersonEntity?> = repository.getPersonById(personId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun insertPerson(person: PersonEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertPerson(person)
            onDone()
        }
    }

    fun updatePerson(person: PersonEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePerson(person)
            onDone()
        }
    }

    fun deletePerson(personId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePerson(personId)
            onDone()
        }
    }

    fun insertRole(role: RoleEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertRole(role)
            onDone()
        }
    }

    fun updateRole(role: RoleEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateRole(role)
            onDone()
        }
    }

    fun deleteRole(roleId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteRole(roleId)
            onDone()
        }
    }
}
