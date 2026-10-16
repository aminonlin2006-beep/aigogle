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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WaterDrop
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
import com.example.data.entities.FarmEntity
import com.example.data.entities.PlotEntity
import com.example.ui.components.PersianLinkedAreaInput
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.AppScreen
import com.example.util.PersianNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmsScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val farms by viewModel.farms.collectAsState()
    val allPlots by viewModel.allPlots.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showInactiveSection by remember { mutableStateOf(false) }

    val activeFarms = farms.filter { it.active }
    val inactiveFarms = farms.filter { !it.active }

    val enabledWorkTypes = remember(settings?.enabledWorkTypesJson) {
        viewModel.parseJsonStringList(settings?.enabledWorkTypesJson ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فهرست مزرعه‌ها", fontWeight = FontWeight.Bold) },
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
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_farm_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن مزرعه")
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

            if (activeFarms.isEmpty() && inactiveFarms.isEmpty()) {
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
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "هنوز مزرعه‌ای ثبت نشده است.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.testTag("empty_add_farm_btn")
                            ) {
                                Text("افزودن اولین مزرعه")
                            }
                        }
                    }
                }
            } else {
                items(activeFarms, key = { it.id }) { farm ->
                    FarmCard(
                        farm = farm,
                        plots = allPlots.filter { it.farmId == farm.id && it.active },
                        onClick = { viewModel.navigateTo(AppScreen.FarmDetail(farm.id)) }
                    )
                }

                // Inactive farms accordion
                if (inactiveFarms.isNotEmpty()) {
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
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "غیرفعال‌ها (${PersianNumberFormatter.formatInteger(inactiveFarms.size.toLong())})",
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
                        items(inactiveFarms, key = { it.id }) { farm ->
                            FarmCard(
                                farm = farm,
                                plots = allPlots.filter { it.farmId == farm.id },
                                isInactive = true,
                                onClick = { viewModel.navigateTo(AppScreen.FarmDetail(farm.id)) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        FarmFormDialog(
            existingFarm = null,
            enabledWorkTypes = enabledWorkTypes,
            onDismiss = { showAddDialog = false },
            onSave = { farm, workType ->
                viewModel.saveFarm(farm, workType) {
                    showAddDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FarmCard(
    farm: FarmEntity,
    plots: List<PlotEntity>,
    isInactive: Boolean = false,
    onClick: () -> Unit
) {
    // Work type calculation:
    // If real plots exist, calculate from active real plots.
    // If no real plots exist, read from default plot.
    val realPlots = plots.filter { !it.isDefault }
    val workTypeLabel = remember(plots) {
        val types = if (realPlots.isNotEmpty()) {
            realPlots.map { it.workType }.toSet()
        } else {
            val def = plots.find { it.isDefault }
            if (def != null) setOf(def.workType) else setOf("farming")
        }
        val hasFarming = types.contains("farming")
        val hasNursery = types.contains("nursery")
        when {
            hasFarming && hasNursery -> "زراعت و نهالستان"
            hasNursery -> "نهالستان"
            else -> "زراعت"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("farm_card_${farm.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactive) 0.dp else 2.dp)
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
                    color = if (isInactive) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                // Work type chip
                SuggestionChip(
                    onClick = {},
                    label = { Text(workTypeLabel) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Area
            if (farm.areaM2 != null && farm.areaM2 > 0) {
                Text(
                    text = "مساحت: ${PersianNumberFormatter.formatAreaBoth(farm.areaM2)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "مساحت: ثبت نشده",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Ownership
                val ownershipText = when (farm.ownership) {
                    "owned" -> "ملکی"
                    "rented" -> "اجاره‌ای"
                    "mixed" -> "ترکیبی"
                    else -> null
                }
                if (ownershipText != null) {
                    Text(
                        text = "مالکیت: $ownershipText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Water source
                val waterText = when (farm.waterSource) {
                    "well" -> "چاه"
                    "canal" -> "نوبت کانال"
                    "both" -> "چاه و کانال"
                    else -> null
                }
                if (waterText != null) {
                    Text(
                        text = "آب: $waterText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FarmFormDialog(
    existingFarm: FarmEntity?,
    enabledWorkTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (FarmEntity, String) -> Unit
) {
    var name by remember { mutableStateOf(existingFarm?.name ?: "") }
    var areaM2 by remember { mutableStateOf<Double?>(existingFarm?.areaM2) }
    var ownership by remember { mutableStateOf(existingFarm?.ownership ?: "owned") }
    var waterSource by remember { mutableStateOf(existingFarm?.waterSource) }
    var note by remember { mutableStateOf(existingFarm?.note ?: "") }
    var active by remember { mutableStateOf(existingFarm?.active ?: true) }

    // Work type selection for new farm:
    // If both types enabled in settings: required choice, default "farming".
    // If only one enabled in settings: auto-select that one and hide selector.
    val hasBothWorkTypes = enabledWorkTypes.contains("farming") && enabledWorkTypes.contains("nursery")
    val defaultWorkType = if (enabledWorkTypes.contains("farming")) "farming" else "nursery"
    var selectedWorkType by remember { mutableStateOf(defaultWorkType) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingFarm == null) "افزودن مزرعه جدید" else "ویرایش مزرعه")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = { Text("اسم مزرعه *") },
                        placeholder = { Text("مثلاً زمین بالایی") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("farm_name_input")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Work Type Selection (only shown when both types are enabled in settings)
                if (existingFarm == null && hasBothWorkTypes) {
                    item {
                        Column {
                            Text("نوع کار اصلی مزرعه:", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { selectedWorkType = "farming" }
                                        .padding(end = 16.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedWorkType == "farming",
                                        onClick = { selectedWorkType = "farming" }
                                    )
                                    Text("زراعت")
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedWorkType = "nursery" }
                                ) {
                                    RadioButton(
                                        selected = selectedWorkType == "nursery",
                                        onClick = { selectedWorkType = "nursery" }
                                    )
                                    Text("نهالستان")
                                }
                            }
                        }
                    }
                }

                // Linked Area Input (ha & m²)
                item {
                    Text("مساحت (اختیاری):", style = MaterialTheme.typography.labelLarge)
                    PersianLinkedAreaInput(
                        initialAreaM2 = areaM2,
                        onAreaM2Changed = { areaM2 = it }
                    )
                }

                // Ownership
                item {
                    Column {
                        Text("مالکیت:", style = MaterialTheme.typography.labelLarge)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                "owned" to "ملکی",
                                "rented" to "اجاره‌ای",
                                "mixed" to "ترکیبی"
                            ).forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { ownership = key }
                                ) {
                                    RadioButton(
                                        selected = ownership == key,
                                        onClick = { ownership = key }
                                    )
                                    Text(label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Water Source
                item {
                    Column {
                        Text("منبع آب:", style = MaterialTheme.typography.labelLarge)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                null to "نامشخص",
                                "well" to "چاه",
                                "canal" to "کانال",
                                "both" to "هر دو"
                            ).forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { waterSource = key }
                                ) {
                                    RadioButton(
                                        selected = waterSource == key,
                                        onClick = { waterSource = key }
                                    )
                                    Text(label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Note
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("توضیحات") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                // Active Switch (in edit mode)
                if (existingFarm != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت مزرعه فعال است:")
                            Switch(
                                checked = active,
                                onCheckedChange = { active = it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().length < 2) {
                        errorMessage = "اسم مزرعه باید حداقل ۲ حرف باشد."
                        return@Button
                    }
                    val farm = (existingFarm ?: FarmEntity(name = name.trim())).copy(
                        name = name.trim(),
                        areaM2 = areaM2,
                        ownership = ownership,
                        waterSource = waterSource,
                        note = note.trim(),
                        active = active
                    )
                    onSave(farm, selectedWorkType)
                },
                modifier = Modifier.testTag("save_farm_btn")
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
