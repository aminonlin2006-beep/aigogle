package com.example.data.repository

import com.example.data.dao.PersonDao
import com.example.data.dao.RoleDao
import com.example.data.entities.PersonEntity
import com.example.data.entities.RoleEntity
import kotlinx.coroutines.flow.Flow

class PersonRepository(
    private val personDao: PersonDao,
    private val roleDao: RoleDao
) {

    val allPeople: Flow<List<PersonEntity>> = personDao.getAllPeople()

    val allRoles: Flow<List<RoleEntity>> = roleDao.getAllRoles()

    val visibleRoles: Flow<List<RoleEntity>> = roleDao.getVisibleRoles()

    fun getPersonById(id: String): Flow<PersonEntity?> = personDao.getPersonById(id)

    suspend fun insertPerson(person: PersonEntity) = personDao.insert(person)

    suspend fun updatePerson(person: PersonEntity) = personDao.update(person)

    suspend fun deletePerson(personId: String) = personDao.softDeletePerson(personId)

    suspend fun insertRole(role: RoleEntity) = roleDao.insert(role)

    suspend fun updateRole(role: RoleEntity) = roleDao.update(role)

    suspend fun deleteRole(roleId: String) = roleDao.softDeleteRole(roleId)
}
