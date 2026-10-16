package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.entities.ActivityEntity
import com.example.data.entities.StageEntity
import com.example.ui.components.ColorPalettePicker
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.PALETTE_12
import com.example.ui.viewmodel.AgriViewModel
import com.example.util.PersianNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val stages by viewModel.stages.collectAsState()
    val activities by viewModel.activities.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddStageDialog by remember { mutableStateOf(false) }
    var stageToEdit by remember { mutableStateOf<StageEntity?>(null) }
    var stageToDelete by remember { mutableStateOf<StageEntity?>(null) }

    var stageForNewActivity by remember { mutableStateOf<StageEntity?>(null) }
    var activityToEdit by remember { mutableStateOf<ActivityEntity?>(null) }
    var activityToDelete by remember { mutableStateOf<ActivityEntity?>(null) }
    var activityToMerge by remember { mutableStateOf<ActivityEntity?>(null) }

    var showHiddenSection by remember { mutableStateOf(false) }

    // Map of expanded stages
    val expandedStages = remember { mutableStateMapOf<String, Boolean>() }

    // Filter by search
    val filteredStages = stages.filter { stage ->
        val stageMatches = stage.name.contains(searchQuery, ignoreCase = true)
        val activitiesForThis = activities.filter { it.stageId == stage.id }
        val activityMatches = activitiesForThis.any { it.name.contains(searchQuery, ignoreCase = true) }
        stageMatches || activityMatches
    }

    val visibleStages = filteredStages.filter { !it.hidden }
    val hiddenStages = filteredStages.filter { it.hidden }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("درخت برچسب‌ها", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStageDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_stage_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن مرحله")
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
            // Search Bar (Section 4-6)
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجو در مرحله‌ها و فعالیت‌ها...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("tags_search_input")
                )
            }

            // Visible Stages
            items(visibleStages, key = { it.id }) { stage ->
                val isExpanded = expandedStages[stage.id] ?: true
                val stageActivities = activities.filter { it.stageId == stage.id }

                StageCard(
                    stage = stage,
                    activities = stageActivities,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedStages[stage.id] = !isExpanded },
                    onEdit = { stageToEdit = stage },
                    onToggleHide = { viewModel.toggleStageHidden(stage.id, !stage.hidden) },
                    onDelete = { stageToDelete = stage },
                    onAddActivity = { stageForNewActivity = stage },
                    onEditActivity = { activityToEdit = it },
                    onDeleteActivity = { activityToDelete = it },
                    onMergeActivity = { activityToMerge = it }
                )
            }

            // Hidden Stages Accordion (Section 4-6)
            if (hiddenStages.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHiddenSection = !showHiddenSection },
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
                                text = "مرحله‌های پنهان (${PersianNumberFormatter.formatInteger(hiddenStages.size.toLong())})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Icon(
                                imageVector = if (showHiddenSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                if (showHiddenSection) {
                    items(hiddenStages, key = { it.id }) { stage ->
                        val isExpanded = expandedStages[stage.id] ?: false
                        val stageActivities = activities.filter { it.stageId == stage.id }

                        StageCard(
                            stage = stage,
                            activities = stageActivities,
                            isExpanded = isExpanded,
                            isStageHidden = true,
                            onToggleExpand = { expandedStages[stage.id] = !isExpanded },
                            onEdit = { stageToEdit = stage },
                            onToggleHide = { viewModel.toggleStageHidden(stage.id, !stage.hidden) },
                            onDelete = { stageToDelete = stage },
                            onAddActivity = { stageForNewActivity = stage },
                            onEditActivity = { activityToEdit = it },
                            onDeleteActivity = { activityToDelete = it },
                            onMergeActivity = { activityToMerge = it }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add / Edit Stage Dialog
    if (showAddStageDialog || stageToEdit != null) {
        val targetStage = stageToEdit
        StageFormDialog(
            existingStage = targetStage,
            usedColors = stages.map { it.color },
            onDismiss = {
                showAddStageDialog = false
                stageToEdit = null
            },
            onSave = { updatedStage ->
                viewModel.saveStage(updatedStage) {
                    showAddStageDialog = false
                    stageToEdit = null
                }
            }
        )
    }

    // Delete Stage Confirmation (Custom stages only; warns that activities under it will be deleted!)
    stageToDelete?.let { stage ->
        val childCount = activities.count { it.stageId == stage.id }
        val msg = if (childCount > 0) {
            "با حذف مرحله «${stage.name}»، تعداد ${PersianNumberFormatter.formatInteger(childCount.toLong())} فعالیت زیر آن نیز حذف می‌شوند. آیا مطمئنید؟"
        } else {
            "آیا از حذف مرحله «${stage.name}» اطمینان دارید؟"
        }

        ConfirmDialog(
            title = "حذف مرحله",
            message = msg,
            confirmText = "حذف مرحله و فعالیت‌ها",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteStage(stage.id) {
                    stageToDelete = null
                }
            },
            onDismiss = { stageToDelete = null }
        )
    }

    // Add / Edit Activity Dialog
    if (stageForNewActivity != null || activityToEdit != null) {
        val targetAct = activityToEdit
        val parentStage = stageForNewActivity ?: stages.find { it.id == targetAct?.stageId }
        if (parentStage != null) {
            ActivityFormDialog(
                existingActivity = targetAct,
                stage = parentStage,
                usedColors = activities.map { it.color },
                onDismiss = {
                    stageForNewActivity = null
                    activityToEdit = null
                },
                onSave = { updatedAct ->
                    viewModel.saveActivity(updatedAct) {
                        stageForNewActivity = null
                        activityToEdit = null
                    }
                }
            )
        }
    }

    // Delete Activity Confirmation
    activityToDelete?.let { act ->
        ConfirmDialog(
            title = "حذف فعالیت",
            message = "آیا از حذف فعالیت «${act.name}» اطمینان دارید؟",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteActivity(act.id) {
                    activityToDelete = null
                }
            },
            onDismiss = { activityToDelete = null }
        )
    }

    // Merge Activity ("یکی کن با...") Dialog (Decisions 124, 164)
    activityToMerge?.let { sourceAct ->
        MergeActivityDialog(
            sourceActivity = sourceAct,
            allActivities = activities.filter { it.id != sourceAct.id },
            stages = stages,
            onDismiss = { activityToMerge = null },
            onConfirmMerge = { targetActId ->
                viewModel.mergeActivities(sourceAct.id, targetActId) {
                    activityToMerge = null
                }
            }
        )
    }
}

@Composable
fun StageCard(
    stage: StageEntity,
    activities: List<ActivityEntity>,
    isExpanded: Boolean,
    isStageHidden: Boolean = false,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onToggleHide: () -> Unit,
    onDelete: () -> Unit,
    onAddActivity: () -> Unit,
    onEditActivity: (ActivityEntity) -> Unit,
    onDeleteActivity: (ActivityEntity) -> Unit,
    onMergeActivity: (ActivityEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("stage_card_${stage.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isStageHidden) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Stage Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Stage Color Dot
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(stage.color))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stage.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isStageHidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${PersianNumberFormatter.formatInteger(activities.size.toLong())} فعالیت)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleHide) {
                        Icon(
                            imageVector = if (stage.hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "پنهان/آشکار",
                            tint = if (stage.hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش مرحله")
                    }

                    if (stage.builtInKey == null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف مرحله", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "باز/بسته"
                        )
                    }
                }
            }

            // Expanded Activities List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activities.forEach { act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(act.color))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = act.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row {
                                // "یکی کن با..." Merge Button
                                IconButton(
                                    onClick = { onMergeActivity(act) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallMerge,
                                        contentDescription = "یکی کردن با...",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onEditActivity(act) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { onDeleteActivity(act) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add Activity Button
                    OutlinedButton(
                        onClick = onAddActivity,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن فعالیت به ${stage.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun StageFormDialog(
    existingStage: StageEntity?,
    usedColors: List<Long>,
    onDismiss: () -> Unit,
    onSave: (StageEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingStage?.name ?: "") }
    // Pick an unused color from palette if available
    val defaultColor = remember(existingStage) {
        existingStage?.color ?: (PALETTE_12.firstOrNull { !usedColors.contains(it) } ?: PALETTE_12.first())
    }
    var selectedColor by remember { mutableStateOf(defaultColor) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingStage == null) "افزودن مرحله تازه" else "ویرایش مرحله") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("نام مرحله *") },
                    placeholder = { Text("مثلاً آماده‌سازی زمین") },
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

                ColorPalettePicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.trim().isBlank()) {
                    errorMessage = "نام مرحله اجباری است."
                    return@Button
                }
                val stage = (existingStage ?: StageEntity(name = name.trim())).copy(
                    name = name.trim(),
                    color = selectedColor
                )
                onSave(stage)
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

@Composable
fun ActivityFormDialog(
    existingActivity: ActivityEntity?,
    stage: StageEntity,
    usedColors: List<Long>,
    onDismiss: () -> Unit,
    onSave: (ActivityEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingActivity?.name ?: "") }
    val defaultColor = remember(existingActivity) {
        existingActivity?.color ?: (PALETTE_12.firstOrNull { !usedColors.contains(it) } ?: stage.color)
    }
    var selectedColor by remember { mutableStateOf(defaultColor) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingActivity == null) "افزودن فعالیت به ${stage.name}" else "ویرایش فعالیت")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("نام فعالیت *") },
                    placeholder = { Text("مثلاً وجین، هرس، سم‌پاشی") },
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

                ColorPalettePicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.trim().isBlank()) {
                    errorMessage = "نام فعالیت اجباری است."
                    return@Button
                }
                val act = (existingActivity ?: ActivityEntity(
                    stageId = stage.id,
                    name = name.trim(),
                    color = selectedColor
                )).copy(
                    name = name.trim(),
                    color = selectedColor
                )
                onSave(act)
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

@Composable
fun MergeActivityDialog(
    sourceActivity: ActivityEntity,
    allActivities: List<ActivityEntity>,
    stages: List<StageEntity>,
    onDismiss: () -> Unit,
    onConfirmMerge: (String) -> Unit
) {
    var selectedTargetId by remember { mutableStateOf<String?>(allActivities.firstOrNull()?.id) }
    val stagesMap = remember(stages) { stages.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("یکی کردن «${sourceActivity.name}» با...") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "با انتخاب یک فعالیت دیگر، فعالیت «${sourceActivity.name}» حذف شده و همه ثبت‌های آن به فعالیت انتخاب‌شده منتقل می‌شوند.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (allActivities.isEmpty()) {
                    Text("فعالیت دیگری برای یکی کردن وجود ندارد.", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(allActivities, key = { it.id }) { act ->
                            val stage = stagesMap[act.stageId]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTargetId = act.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTargetId == act.id,
                                    onClick = { selectedTargetId = act.id }
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(act.color))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${act.name} (زیر مرحله ${stage?.name ?: ""})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTargetId?.let { onConfirmMerge(it) }
                },
                enabled = selectedTargetId != null
            ) {
                Text("یکی کن و حذف")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
