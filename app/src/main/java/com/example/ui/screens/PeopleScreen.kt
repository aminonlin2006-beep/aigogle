package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entities.PersonEntity
import com.example.data.entities.RoleEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.viewmodel.AgriViewModel
import com.example.util.PersianNumberFormatter
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val people by viewModel.people.collectAsState()
    val roles by viewModel.roles.collectAsState()

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<PersonEntity?>(null) }
    var personToDelete by remember { mutableStateOf<PersonEntity?>(null) }
    var showRolesDialog by remember { mutableStateOf(false) }
    var showInactiveSection by remember { mutableStateOf(false) }

    val activePeople = people.filter { it.active }
    val inactivePeople = people.filter { !it.active }

    val rolesMap = remember(roles) { roles.associateBy { it.id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اشخاص و نقش‌ها", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { showRolesDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("manage_roles_btn")
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نقش‌ها")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPersonDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_person_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن شخص")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (activePeople.isEmpty() && inactivePeople.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "هنوز شخصی ثبت نشده است.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddPersonDialog = true },
                                modifier = Modifier.testTag("empty_add_person_btn")
                            ) {
                                Text("افزودن اولین شخص")
                            }
                        }
                    }
                }
            } else {
                items(activePeople, key = { it.id }) { person ->
                    PersonCard(
                        person = person,
                        rolesMap = rolesMap,
                        viewModel = viewModel,
                        onEdit = { personToEdit = person },
                        onDelete = { personToDelete = person }
                    )
                }

                // Inactive section
                if (inactivePeople.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showInactiveSection = !showInactiveSection },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "غیرفعال‌ها (${PersianNumberFormatter.formatInteger(inactivePeople.size.toLong())})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Icon(
                                    imageVector = if (showInactiveSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    if (showInactiveSection) {
                        items(inactivePeople, key = { it.id }) { person ->
                            PersonCard(
                                person = person,
                                rolesMap = rolesMap,
                                viewModel = viewModel,
                                isInactive = true,
                                onEdit = { personToEdit = person },
                                onDelete = { personToDelete = person }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add Person Dialog
    if (showAddPersonDialog) {
        PersonFormDialog(
            existingPerson = null,
            roles = roles,
            viewModel = viewModel,
            onDismiss = { showAddPersonDialog = false },
            onSave = { person ->
                viewModel.savePerson(person) {
                    showAddPersonDialog = false
                }
            }
        )
    }

    // Edit Person Dialog
    personToEdit?.let { person ->
        PersonFormDialog(
            existingPerson = person,
            roles = roles,
            viewModel = viewModel,
            onDismiss = { personToEdit = null },
            onSave = { updated ->
                viewModel.savePerson(updated) {
                    personToEdit = null
                }
            }
        )
    }

    // Delete Person Dialog
    personToDelete?.let { person ->
        ConfirmDialog(
            title = "حذف شخص",
            message = "آیا از حذف «${person.name}» اطمینان دارید؟",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePerson(person.id) {
                    personToDelete = null
                }
            },
            onDismiss = { personToDelete = null }
        )
    }

    // Roles Management Dialog
    if (showRolesDialog) {
        ManageRolesDialog(
            roles = roles,
            viewModel = viewModel,
            onDismiss = { showRolesDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonCard(
    person: PersonEntity,
    rolesMap: Map<String, RoleEntity>,
    viewModel: AgriViewModel,
    isInactive: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val assignedRoleIds = remember(person.roleIdsJson) {
        viewModel.parseJsonStringList(person.roleIdsJson)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onEdit)
            .testTag("person_card_${person.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactive) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isInactive) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )

                if (person.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = PersianNumberFormatter.formatDigits(person.phone),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (assignedRoleIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        assignedRoleIds.forEach { roleId ->
                            val role = rolesMap[roleId]
                            if (role != null) {
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = role.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (role.hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }

                if (person.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = person.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonFormDialog(
    existingPerson: PersonEntity?,
    roles: List<RoleEntity>,
    viewModel: AgriViewModel,
    onDismiss: () -> Unit,
    onSave: (PersonEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingPerson?.name ?: "") }
    var phone by remember { mutableStateOf(existingPerson?.phone ?: "") }
    var note by remember { mutableStateOf(existingPerson?.note ?: "") }
    var active by remember { mutableStateOf(existingPerson?.active ?: true) }

    val initialSelectedRoleIds = remember(existingPerson?.roleIdsJson) {
        viewModel.parseJsonStringList(existingPerson?.roleIdsJson ?: "").toSet()
    }
    var selectedRoleIds by remember { mutableStateOf(initialSelectedRoleIds) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Visible roles for selection: hidden roles are not offered to new people (Decision پ8),
    // but if the person already has the role, it is still visible (dimmed).
    val selectableRoles = roles.filter { !it.hidden || selectedRoleIds.contains(it.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingPerson == null) "افزودن شخص جدید" else "ویرایش شخص")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name (Mandatory)
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = { Text("نام و نام خانوادگی *") },
                        placeholder = { Text("مثلاً علی رضایی") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("person_name_input")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Phone (Optional, accepts Persian and English numbers)
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("شماره تلفن (اختیاری)") },
                        placeholder = { Text("مثلاً ۰۹۱۲۳۴۵۶۷۸۹") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("person_phone_input")
                    )
                }

                // Roles Selection (Multi-select)
                item {
                    Text("نقش‌ها:", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectableRoles.forEach { role ->
                            val isSelected = selectedRoleIds.contains(role.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newSet = selectedRoleIds.toMutableSet()
                                    if (isSelected) newSet.remove(role.id) else newSet.add(role.id)
                                    selectedRoleIds = newSet
                                },
                                label = {
                                    Text(
                                        text = role.name + if (role.hidden) " (پنهان)" else "",
                                        color = if (role.hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }

                // Note
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("توضیحات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Active switch in edit mode
                if (existingPerson != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت شخص فعال است:")
                            Switch(checked = active, onCheckedChange = { active = it })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) {
                        errorMessage = "نام شخص اجباری است."
                        return@Button
                    }
                    val person = (existingPerson ?: PersonEntity(name = name.trim())).copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        roleIdsJson = JSONArray(selectedRoleIds.toList()).toString(),
                        note = note.trim(),
                        active = active
                    )
                    onSave(person)
                },
                modifier = Modifier.testTag("save_person_btn")
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun ManageRolesDialog(
    roles: List<RoleEntity>,
    viewModel: AgriViewModel,
    onDismiss: () -> Unit
) {
    var showAddRoleDialog by remember { mutableStateOf(false) }
    var roleToEdit by remember { mutableStateOf<RoleEntity?>(null) }
    var roleToDelete by remember { mutableStateOf<RoleEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("مدیریت نقش‌ها", fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showAddRoleDialog = true },
                    modifier = Modifier.testTag("add_role_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نقش تازه")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roles, key = { it.id }) { role ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (role.hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (role.builtInKey != null) "نقش آماده سامانه" else "نقش ساخته‌ی کاربر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Hide / Unhide button
                                IconButton(onClick = { viewModel.toggleRoleHidden(role) }) {
                                    Icon(
                                        imageVector = if (role.hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "پنهان/آشکار",
                                        tint = if (role.hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Edit button
                                IconButton(onClick = { roleToEdit = role }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }

                                // Delete button (only for custom roles! Built-in cannot be deleted; Decisions 118, 165)
                                if (role.builtInKey == null) {
                                    IconButton(onClick = { roleToDelete = role }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )

    // Add / Edit Role Dialog
    if (showAddRoleDialog || roleToEdit != null) {
        val targetRole = roleToEdit
        RoleFormDialog(
            existingRole = targetRole,
            onDismiss = {
                showAddRoleDialog = false
                roleToEdit = null
            },
            onSave = { updatedRole ->
                viewModel.saveRole(updatedRole) {
                    showAddRoleDialog = false
                    roleToEdit = null
                }
            }
        )
    }

    // Delete Role Confirmation
    roleToDelete?.let { role ->
        ConfirmDialog(
            title = "حذف نقش",
            message = "آیا از حذف نقش «${role.name}» اطمینان دارید؟",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteRole(role.id) {
                    roleToDelete = null
                }
            },
            onDismiss = { roleToDelete = null }
        )
    }
}

@Composable
fun RoleFormDialog(
    existingRole: RoleEntity?,
    onDismiss: () -> Unit,
    onSave: (RoleEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingRole?.name ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingRole == null) "افزودن نقش تازه" else "ویرایش نقش")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("نام نقش *") },
                    placeholder = { Text("مثلاً راننده، نگهبان") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.trim().isBlank()) {
                    errorMessage = "نام نقش اجباری است."
                    return@Button
                }
                val role = (existingRole ?: RoleEntity(name = name.trim())).copy(name = name.trim())
                onSave(role)
            }) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
