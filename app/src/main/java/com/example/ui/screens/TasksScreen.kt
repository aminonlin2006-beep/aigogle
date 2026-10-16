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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entities.ActivityEntity
import com.example.data.entities.FarmEntity
import com.example.data.entities.PersonEntity
import com.example.data.entities.PlotEntity
import com.example.data.entities.StageEntity
import com.example.data.entities.TaskEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.viewmodel.AgriViewModel
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberFormatter
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val farms by viewModel.farms.collectAsState()
    val allPlots by viewModel.allPlots.collectAsState()
    val people by viewModel.people.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val activities by viewModel.activities.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToReschedule by remember { mutableStateOf<TaskEntity?>(null) }

    val todayKey = remember { PersianDateUtils.today().toKey() }

    // Tabs: 0: امروز, 1: فردا, 2: عقب‌افتاده, 3: همه در انتظار, 4: انجام‌شده
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("امروز", "عقب‌افتاده", "همه آینده", "انجام‌شده")

    val filteredTasks = remember(allTasks, selectedTab, todayKey) {
        when (selectedTab) {
            0 -> allTasks.filter { it.date == todayKey && it.status == "pending" }
            1 -> allTasks.filter { it.date < todayKey && it.status == "pending" }
            2 -> allTasks.filter { it.status == "pending" }
            3 -> allTasks.filter { it.status == "completed" }
            else -> allTasks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("کارهای آینده و برنامه روزانه", fontWeight = FontWeight.Bold) },
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
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "تعریف کار جدید")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            val count = when (index) {
                                0 -> allTasks.count { it.date == todayKey && it.status == "pending" }
                                1 -> allTasks.count { it.date < todayKey && it.status == "pending" }
                                2 -> allTasks.count { it.status == "pending" }
                                3 -> allTasks.count { it.status == "completed" }
                                else -> 0
                            }
                            Text(
                                text = "$title (${PersianNumberFormatter.formatInteger(count.toLong())})",
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> "برای امروز کاری ثبت نشده است."
                                1 -> "کار عقب‌افتاده‌ای وجود ندارد."
                                2 -> "هیچ کار در انتظاری ثبت نشده است."
                                else -> "کاری در این بخش وجود ندارد."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.testTag("empty_add_task_btn")
                        ) {
                            Text("افزودن کار تازه")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(filteredTasks, key = { it.id }) { task ->
                        val farm = farms.find { it.id == task.farmId }
                        val plot = allPlots.find { it.id == task.plotId }
                        val stage = stages.find { it.id == task.stageId }
                        val activity = activities.find { it.id == task.activityId }

                        TaskCardItem(
                            task = task,
                            farm = farm,
                            plot = plot,
                            stage = stage,
                            activity = activity,
                            allPeople = people,
                            viewModel = viewModel,
                            onComplete = { viewModel.markTaskCompleted(task.id) },
                            onCancel = { viewModel.markTaskCancelled(task.id) },
                            onReschedule = { taskToReschedule = task },
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDelete = task }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        TaskFormDialog(
            existingTask = null,
            farms = farms,
            allPlots = allPlots,
            people = people,
            stages = stages,
            activities = activities,
            viewModel = viewModel,
            onDismiss = { showAddTaskDialog = false },
            onSave = { newTask ->
                viewModel.saveTask(newTask) {
                    showAddTaskDialog = false
                }
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        TaskFormDialog(
            existingTask = task,
            farms = farms,
            allPlots = allPlots,
            people = people,
            stages = stages,
            activities = activities,
            viewModel = viewModel,
            onDismiss = { taskToEdit = null },
            onSave = { updated ->
                viewModel.saveTask(updated) {
                    taskToEdit = null
                }
            }
        )
    }

    // Delete Task Confirmation
    taskToDelete?.let { task ->
        ConfirmDialog(
            title = "حذف کار",
            message = "آیا از حذف کار «${task.title}» اطمینان دارید؟",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTask(task.id) {
                    taskToDelete = null
                }
            },
            onDismiss = { taskToDelete = null }
        )
    }

    // Reschedule Task Dialog
    taskToReschedule?.let { task ->
        RescheduleDialog(
            task = task,
            onDismiss = { taskToReschedule = null },
            onConfirm = { newDateKey ->
                viewModel.rescheduleTask(task.id, newDateKey)
                taskToReschedule = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCardItem(
    task: TaskEntity,
    farm: FarmEntity?,
    plot: PlotEntity?,
    stage: StageEntity?,
    activity: ActivityEntity?,
    allPeople: List<PersonEntity>,
    viewModel: AgriViewModel,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "urgent" -> MaterialTheme.colorScheme.error
        "important" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    val priorityLabel = when (task.priority) {
        "urgent" -> "فوری"
        "important" -> "مهم"
        else -> "عادی"
    }

    val isCompleted = task.status == "completed"

    val assignedPeopleNames = remember(task.assignedPersonIdsJson, allPeople) {
        val ids = viewModel.parseJsonStringList(task.assignedPersonIdsJson)
        allPeople.filter { ids.contains(it.id) }.map { it.name }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Priority Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                }

                SuggestionChip(
                    onClick = {},
                    label = { Text(priorityLabel) },
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Date & Farm/Plot Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = PersianNumberFormatter.formatDigits(task.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (farm != null) {
                    Text(
                        text = "• ${farm.name}" + if (plot != null && !plot.isDefault) " / ${plot.name}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Work volume estimation (Decision 55)
            if (task.manDays != null && task.manDays > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                val manDaysStr = PersianNumberFormatter.formatDecimal(task.manDays, 1)
                val personCountStr = task.personCount?.let { PersianNumberFormatter.formatInteger(it.toLong()) }
                val estimatedDaysStr = task.estimatedDays?.let { PersianNumberFormatter.formatDecimal(it, 1) }

                Text(
                    text = "حجم: $manDaysStr نفرروز" +
                            if (personCountStr != null && estimatedDaysStr != null) " (با $personCountStr نفر ← $estimatedDaysStr روز)" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Stage / Activity badge
            if (stage != null || activity != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val labelText = if (activity != null) "${stage?.name ?: ""}: ${activity.name}" else (stage?.name ?: "")
                    SuggestionChip(
                        onClick = {},
                        label = { Text(labelText, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            // Assignees
            if (assignedPeopleNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "انجام‌دهنده: ${assignedPeopleNames.joinToString("، ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (task.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCompleted) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.testTag("complete_task_${task.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("انجام شد")
                    }

                    OutlinedButton(
                        onClick = onReschedule,
                        modifier = Modifier.testTag("reschedule_task_${task.id}")
                    ) {
                        Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تغییر تاریخ")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("انجام شده", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormDialog(
    existingTask: TaskEntity?,
    farms: List<FarmEntity>,
    allPlots: List<PlotEntity>,
    people: List<PersonEntity>,
    stages: List<StageEntity>,
    activities: List<ActivityEntity>,
    viewModel: AgriViewModel,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var selectedFarmId by remember { mutableStateOf(existingTask?.farmId) }
    var selectedPlotId by remember { mutableStateOf(existingTask?.plotId) }
    var dateKey by remember { mutableStateOf(existingTask?.date ?: PersianDateUtils.today().toKey()) }
    var priority by remember { mutableStateOf(existingTask?.priority ?: "normal") }
    var manDaysText by remember { mutableStateOf(existingTask?.manDays?.let { PersianNumberFormatter.formatDecimal(it, 1) } ?: "") }
    var personCountText by remember { mutableStateOf(existingTask?.personCount?.toString() ?: "") }
    var selectedStageId by remember { mutableStateOf(existingTask?.stageId) }
    var selectedActivityId by remember { mutableStateOf(existingTask?.activityId) }
    var note by remember { mutableStateOf(existingTask?.note ?: "") }

    val initialAssigned = remember(existingTask?.assignedPersonIdsJson) {
        viewModel.parseJsonStringList(existingTask?.assignedPersonIdsJson ?: "").toSet()
    }
    var assignedPersonIds by remember { mutableStateOf(initialAssigned) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val availablePlots = remember(selectedFarmId, allPlots) {
        if (selectedFarmId == null) emptyList()
        else allPlots.filter { it.farmId == selectedFarmId && !it.isDefault }
    }

    val availableActivities = remember(selectedStageId, activities) {
        if (selectedStageId == null) emptyList()
        else activities.filter { it.stageId == selectedStageId }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTask == null) "تعریف کار جدید" else "ویرایش کار") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("عنوان کار *") },
                        placeholder = { Text("مثلاً وجین ردیف‌های زیتون، سم‌پاشی گندم") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Date Quick Pickers (امروز، فردا، هفته بعد)
                item {
                    Column {
                        Text("تاریخ انجام:", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val today = PersianDateUtils.today().toKey()
                            FilterChip(
                                selected = dateKey == today,
                                onClick = { dateKey = today },
                                label = { Text("امروز") }
                            )
                            // Tomorrow approximation
                            val tomorrowDate = remember {
                                val t = PersianDateUtils.today()
                                "${t.year}-${t.month.toString().padStart(2, '0')}-${(t.day + 1).toString().padStart(2, '0')}"
                            }
                            FilterChip(
                                selected = dateKey == tomorrowDate,
                                onClick = { dateKey = tomorrowDate },
                                label = { Text("فردا") }
                            )
                        }
                        OutlinedTextField(
                            value = PersianNumberFormatter.formatDigits(dateKey),
                            onValueChange = { dateKey = PersianNumberFormatter.normalizeToEnglishDigits(it) },
                            label = { Text("تاریخ جلالی (سال-ماه-روز)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Priority Radio Buttons (Decision 49)
                item {
                    Column {
                        Text("اولویت کار:", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "normal" to "عادی",
                                "important" to "مهم",
                                "urgent" to "فوری"
                            ).forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { priority = key }
                                ) {
                                    RadioButton(selected = priority == key, onClick = { priority = key })
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // Farm & Plot Pickers
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("انتخاب مزرعه و قطعه:", style = MaterialTheme.typography.labelLarge)
                        // Farm Picker Chips
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            farms.forEach { f ->
                                FilterChip(
                                    selected = selectedFarmId == f.id,
                                    onClick = {
                                        selectedFarmId = if (selectedFarmId == f.id) null else f.id
                                        selectedPlotId = null
                                    },
                                    label = { Text(f.name) }
                                )
                            }
                        }

                        // Plot Picker Chips
                        if (availablePlots.isNotEmpty()) {
                            Text("قطعه:", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                availablePlots.forEach { p ->
                                    FilterChip(
                                        selected = selectedPlotId == p.id,
                                        onClick = { selectedPlotId = if (selectedPlotId == p.id) null else p.id },
                                        label = { Text(p.name) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Volume & Workers Estimation (Decision 55)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("برآورد حجم کار و نیرو:", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manDaysText,
                                onValueChange = { manDaysText = it },
                                label = { Text("نفرروز") },
                                placeholder = { Text("مثلاً ۳") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = personCountText,
                                onValueChange = { personCountText = it },
                                label = { Text("تعداد نفر") },
                                placeholder = { Text("مثلاً ۲") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val parsedManDays = PersianNumberFormatter.parseToDouble(manDaysText)
                        val parsedPersons = PersianNumberFormatter.parseToLong(personCountText)?.toInt()
                        if (parsedManDays != null && parsedPersons != null && parsedPersons > 0) {
                            val days = parsedManDays / parsedPersons
                            Text(
                                text = "← زمان لازم: ${PersianNumberFormatter.formatDecimal(days, 1)} روز کاری",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Stage & Activity Picker
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("مرحله و فعالیت:", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            stages.forEach { s ->
                                FilterChip(
                                    selected = selectedStageId == s.id,
                                    onClick = {
                                        selectedStageId = if (selectedStageId == s.id) null else s.id
                                        selectedActivityId = null
                                    },
                                    label = { Text(s.name) }
                                )
                            }
                        }

                        if (availableActivities.isNotEmpty()) {
                            Text("فعالیت:", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                availableActivities.forEach { a ->
                                    FilterChip(
                                        selected = selectedActivityId == a.id,
                                        onClick = { selectedActivityId = if (selectedActivityId == a.id) null else a.id },
                                        label = { Text(a.name) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Assignee People Picker
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("انجام‌دهنده (کارگر یا دسته):", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            people.forEach { p ->
                                val isSelected = assignedPersonIds.contains(p.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val newSet = assignedPersonIds.toMutableSet()
                                        if (isSelected) newSet.remove(p.id) else newSet.add(p.id)
                                        assignedPersonIds = newSet
                                    },
                                    label = { Text(p.name) }
                                )
                            }
                        }
                    }
                }

                // Note
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("توضیحات تکمیلی") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        errorMessage = "عنوان کار اجباری است."
                        return@Button
                    }
                    val manDays = PersianNumberFormatter.parseToDouble(manDaysText)
                    val persons = PersianNumberFormatter.parseToLong(personCountText)?.toInt()

                    val task = (existingTask ?: TaskEntity(
                        title = title.trim(),
                        date = dateKey
                    )).copy(
                        title = title.trim(),
                        farmId = selectedFarmId,
                        plotId = selectedPlotId,
                        date = dateKey,
                        priority = priority,
                        manDays = manDays,
                        personCount = persons,
                        stageId = selectedStageId,
                        activityId = selectedActivityId,
                        assignedPersonIdsJson = JSONArray(assignedPersonIds.toList()).toString(),
                        note = note.trim()
                    )
                    onSave(task)
                },
                modifier = Modifier.testTag("save_task_btn")
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
fun RescheduleDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val today = PersianDateUtils.today()
    val tomorrowKey = remember {
        "${today.year}-${today.month.toString().padStart(2, '0')}-${(today.day + 1).toString().padStart(2, '0')}"
    }
    val in3DaysKey = remember {
        "${today.year}-${today.month.toString().padStart(2, '0')}-${(today.day + 3).toString().padStart(2, '0')}"
    }

    var selectedDate by remember { mutableStateOf(tomorrowKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعویق و تغییر تاریخ کار") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("کار: «${task.title}»")
                Text("تاریخ جدید را انتخاب کنید:")
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedDate == tomorrowKey,
                        onClick = { selectedDate = tomorrowKey },
                        label = { Text("فردا") }
                    )
                    FilterChip(
                        selected = selectedDate == in3DaysKey,
                        onClick = { selectedDate = in3DaysKey },
                        label = { Text("۳ روز بعد") }
                    )
                }

                OutlinedTextField(
                    value = PersianNumberFormatter.formatDigits(selectedDate),
                    onValueChange = { selectedDate = PersianNumberFormatter.normalizeToEnglishDigits(it) },
                    label = { Text("تاریخ جدید (سال-ماه-روز)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDate) }) {
                Text("تأیید تاریخ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
