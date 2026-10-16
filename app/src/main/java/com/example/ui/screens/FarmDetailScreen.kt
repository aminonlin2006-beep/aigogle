package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entities.FarmEntity
import com.example.data.entities.PlotEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.PersianLinkedAreaInput
import com.example.ui.viewmodel.AgriViewModel
import com.example.util.PersianNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDetailScreen(
    farmId: String,
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val farms by viewModel.farms.collectAsState()
    val allPlots by viewModel.allPlots.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val farm = farms.find { it.id == farmId }

    // Plots for this farm (only real plots, default hidden plot is NEVER shown in UI)
    val farmPlots = allPlots.filter { it.farmId == farmId }
    val defaultPlot = farmPlots.find { it.isDefault }
    val realPlots = farmPlots.filter { !it.isDefault }
    val activeRealPlots = realPlots.filter { it.active }
    val inactiveRealPlots = realPlots.filter { !it.active }

    val enabledWorkTypes = remember(settings?.enabledWorkTypesJson) {
        viewModel.parseJsonStringList(settings?.enabledWorkTypesJson ?: "")
    }

    var showEditFarmDialog by remember { mutableStateOf(false) }
    var showDeleteFarmDialog by remember { mutableStateOf(false) }
    var plotToEdit by remember { mutableStateOf<PlotEntity?>(null) }
    var showAddPlotDialog by remember { mutableStateOf(false) }
    var plotToDelete by remember { mutableStateOf<PlotEntity?>(null) }
    var showInactivePlots by remember { mutableStateOf(false) }

    if (farm == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("مزرعه یافت نشد") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                Text("این مزرعه حذف شده یا وجود ندارد.")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(farm.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditFarmDialog = true },
                        modifier = Modifier.testTag("edit_farm_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش مزرعه")
                    }
                    IconButton(
                        onClick = { showDeleteFarmDialog = true },
                        modifier = Modifier.testTag("delete_farm_btn")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف مزرعه",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPlotDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_plot_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن قطعه")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Farm Info Header Card (Section 4-4)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = farm.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (!farm.active) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("غیرفعال") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Area in both units: e.g. "۲٫۵ هکتار (۲۵٬۰۰۰ متر مربع)"
                        if (farm.areaM2 != null && farm.areaM2 > 0) {
                            Text(
                                text = "مساحت: ${PersianNumberFormatter.formatAreaBoth(farm.areaM2)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = "مساحت مزرعه: ثبت نشده",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val ownershipLabel = when (farm.ownership) {
                                "owned" -> "ملکی"
                                "rented" -> "اجاره‌ای"
                                "mixed" -> "ترکیبی"
                                else -> "نامشخص"
                            }
                            Text(
                                text = "مالکیت: $ownershipLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            val waterLabel = when (farm.waterSource) {
                                "well" -> "چاه"
                                "canal" -> "نوبت کانال"
                                "both" -> "چاه و کانال"
                                else -> "نامشخص"
                            }
                            Text(
                                text = "آب: $waterLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (farm.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "توضیح: ${farm.note}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Real Plots List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قطعه‌های مزرعه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showAddPlotDialog = true },
                        modifier = Modifier.testTag("add_plot_btn_header")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن قطعه")
                    }
                }
            }

            // Empty state for real plots
            if (realPlots.isEmpty()) {
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
                                imageVector = Icons.Default.Landscape,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "هنوز قطعه‌ای برای این مزرعه تعریف نشده است.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddPlotDialog = true },
                                modifier = Modifier.testTag("empty_add_plot_btn")
                            ) {
                                Text("افزودن اولین قطعه")
                            }
                        }
                    }
                }
            } else {
                items(activeRealPlots, key = { it.id }) { plot ->
                    PlotCard(
                        plot = plot,
                        onEdit = { plotToEdit = plot },
                        onDelete = { plotToDelete = plot }
                    )
                }

                // Inactive plots accordion
                if (inactiveRealPlots.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showInactivePlots = !showInactivePlots },
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
                                    text = "قطعه‌های غیرفعال (${PersianNumberFormatter.formatInteger(inactiveRealPlots.size.toLong())})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Icon(
                                    imageVector = if (showInactivePlots) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    if (showInactivePlots) {
                        items(inactiveRealPlots, key = { it.id }) { plot ->
                            PlotCard(
                                plot = plot,
                                isInactive = true,
                                onEdit = { plotToEdit = plot },
                                onDelete = { plotToDelete = plot }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Edit Farm Dialog
    if (showEditFarmDialog) {
        FarmFormDialog(
            existingFarm = farm,
            enabledWorkTypes = enabledWorkTypes,
            onDismiss = { showEditFarmDialog = false },
            onSave = { updated, _ ->
                viewModel.saveFarm(updated) {
                    showEditFarmDialog = false
                }
            }
        )
    }

    // Delete Farm Confirmation Dialog (Decision 158: warns if farm has real plots)
    if (showDeleteFarmDialog) {
        val count = realPlots.size
        val warningMessage = if (count > 0) {
            "این مزرعه ${PersianNumberFormatter.formatInteger(count.toLong())} قطعه دارد که همه حذف می‌شوند. آیا مطمئنید؟"
        } else {
            "آیا از حذف این مزرعه اطمینان دارید؟"
        }

        ConfirmDialog(
            title = "حذف مزرعه",
            message = warningMessage,
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteFarm(farm.id) {
                    showDeleteFarmDialog = false
                    viewModel.navigateBack()
                }
            },
            onDismiss = { showDeleteFarmDialog = false }
        )
    }

    // Add Plot Dialog
    if (showAddPlotDialog) {
        val defaultWorkType = defaultPlot?.workType ?: "farming"
        PlotFormDialog(
            existingPlot = null,
            farmId = farm.id,
            defaultWorkType = defaultWorkType,
            enabledWorkTypes = enabledWorkTypes,
            onDismiss = { showAddPlotDialog = false },
            onSave = { newPlot ->
                viewModel.savePlot(newPlot) {
                    showAddPlotDialog = false
                }
            }
        )
    }

    // Edit Plot Dialog
    plotToEdit?.let { plot ->
        PlotFormDialog(
            existingPlot = plot,
            farmId = farm.id,
            defaultWorkType = plot.workType,
            enabledWorkTypes = enabledWorkTypes,
            onDismiss = { plotToEdit = null },
            onSave = { updatedPlot ->
                viewModel.savePlot(updatedPlot) {
                    plotToEdit = null
                }
            }
        )
    }

    // Delete Plot Dialog
    plotToDelete?.let { plot ->
        ConfirmDialog(
            title = "حذف قطعه",
            message = "آیا از حذف قطعه «${plot.name}» اطمینان دارید؟",
            confirmText = "حذف",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePlot(plot.id) {
                    plotToDelete = null
                }
            },
            onDismiss = { plotToDelete = null }
        )
    }
}

@Composable
fun PlotCard(
    plot: PlotEntity,
    isInactive: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onEdit)
            .testTag("plot_card_${plot.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactive) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isInactive) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(if (plot.workType == "nursery") "نهالستان" else "زراعت")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (plot.areaM2 != null && plot.areaM2 > 0) {
                    Text(
                        text = "مساحت: ${PersianNumberFormatter.formatAreaBoth(plot.areaM2)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (plot.note.isNotBlank()) {
                    Text(
                        text = plot.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_plot_${plot.id}")) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش قطعه")
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_plot_${plot.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف قطعه", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PlotFormDialog(
    existingPlot: PlotEntity?,
    farmId: String,
    defaultWorkType: String,
    enabledWorkTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (PlotEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingPlot?.name ?: "") }
    var areaM2 by remember { mutableStateOf<Double?>(existingPlot?.areaM2) }
    var workType by remember { mutableStateOf(existingPlot?.workType ?: defaultWorkType) }
    var note by remember { mutableStateOf(existingPlot?.note ?: "") }
    var active by remember { mutableStateOf(existingPlot?.active ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val hasBothWorkTypes = enabledWorkTypes.contains("farming") && enabledWorkTypes.contains("nursery")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingPlot == null) "افزودن قطعه جدید" else "ویرایش قطعه")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name (Mandatory, Mandate پ2)
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = { Text("نام قطعه *") },
                        placeholder = { Text("مثلاً قطعه یک") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plot_name_input")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Work Type: Only shown if both types are enabled in settings
                if (hasBothWorkTypes) {
                    item {
                        Column {
                            Text("نوع کار قطعه:", style = MaterialTheme.typography.labelLarge)
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { workType = "farming" }
                                        .padding(end = 16.dp)
                                ) {
                                    RadioButton(selected = workType == "farming", onClick = { workType = "farming" })
                                    Text("زراعت")
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { workType = "nursery" }
                                ) {
                                    RadioButton(selected = workType == "nursery", onClick = { workType = "nursery" })
                                    Text("نهالستان")
                                }
                            }
                        }
                    }
                }

                // Linked Area Input (ha & m²)
                item {
                    Text("مساحت قطعه (اختیاری):", style = MaterialTheme.typography.labelLarge)
                    PersianLinkedAreaInput(
                        initialAreaM2 = areaM2,
                        onAreaM2Changed = { areaM2 = it }
                    )
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

                // Active Switch (in edit mode)
                if (existingPlot != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت قطعه فعال است:")
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
                        errorMessage = "نام قطعه اجباری است."
                        return@Button
                    }
                    val plot = (existingPlot ?: PlotEntity(
                        farmId = farmId,
                        name = name.trim()
                    )).copy(
                        name = name.trim(),
                        workType = workType,
                        areaM2 = areaM2,
                        note = note.trim(),
                        active = active,
                        isDefault = false
                    )
                    onSave(plot)
                },
                modifier = Modifier.testTag("save_plot_btn")
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
