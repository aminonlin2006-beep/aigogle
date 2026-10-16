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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.AppScreen
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberFormatter

data class TileInfo(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val screen: AppScreen,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val meta by viewModel.meta.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val deviceSettings by viewModel.deviceSettings.collectAsState()
    val farms by viewModel.farms.collectAsState()
    val people by viewModel.people.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()

    val unsentCount = meta?.unsentChanges ?: 0
    var reminderDismissed by remember { mutableStateOf(false) }
    var isEditLayoutMode by remember { mutableStateOf(false) }

    val todayJalali = remember { PersianDateUtils.today().toLongFormat() }

    // Tile keys
    val allTileKeys = listOf("tasks", "farms", "people", "tags")
    val savedOrder = remember(deviceSettings?.tileLayoutJson) {
        val list = viewModel.parseJsonStringList(deviceSettings?.tileLayoutJson ?: "").toMutableList()
        if (list.isEmpty()) {
            allTileKeys
        } else {
            allTileKeys.forEach { key ->
                if (!list.contains(key)) list.add(0, key)
            }
            list
        }
    }
    val hiddenTiles = remember(deviceSettings?.hiddenTilesJson) {
        viewModel.parseJsonStringList(deviceSettings?.hiddenTilesJson ?: "").toSet()
    }

    val tileInfoMap = mapOf(
        "tasks" to TileInfo(
            key = "tasks",
            title = "کارهای آینده",
            subtitle = if (pendingTasks.isEmpty()) "هیچ کار در انتظاری نیست" else "${PersianNumberFormatter.formatInteger(pendingTasks.size.toLong())} کار در انتظار",
            icon = Icons.Default.EventNote,
            screen = AppScreen.Tasks,
            count = pendingTasks.size
        ),
        "farms" to TileInfo(
            key = "farms",
            title = "مزرعه‌ها",
            subtitle = "${PersianNumberFormatter.formatInteger(farms.size.toLong())} مزرعه ثبت شده",
            icon = Icons.Default.Agriculture,
            screen = AppScreen.Farms,
            count = farms.size
        ),
        "people" to TileInfo(
            key = "people",
            title = "اشخاص",
            subtitle = "${PersianNumberFormatter.formatInteger(people.size.toLong())} نفر ثبت شده",
            icon = Icons.Default.Group,
            screen = AppScreen.People,
            count = people.size
        ),
        "tags" to TileInfo(
            key = "tags",
            title = "برچسب‌ها",
            subtitle = "${PersianNumberFormatter.formatInteger(stages.size.toLong())} مرحله کاری",
            icon = Icons.Default.Label,
            screen = AppScreen.Tags,
            count = stages.size
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = todayJalali,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مدیریت زراعت و نهالستان",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Backup icon with counter badge
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Backup) },
                        modifier = Modifier.testTag("backup_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unsentCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ) {
                                        Text(PersianNumberFormatter.formatInteger(unsentCount.toLong()))
                                    }
                                }
                            }
                        ) {
                            if (unsentCount == 0) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "پشتیبان به‌روز است",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "نیاز به پشتیبان‌گیری",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Settings icon
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Settings) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Startup reminder banner if unsent changes > 0 (Section 4-1)
            if (unsentCount > 0 && !reminderDismissed) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${PersianNumberFormatter.formatInteger(unsentCount.toLong())} تغییر هنوز پشتیبان بیرونی ندارد",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.Backup) },
                                modifier = Modifier.testTag("send_backup_banner_btn")
                            ) {
                                Text("فرستادن")
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { reminderDismissed = true },
                                modifier = Modifier.testTag("dismiss_banner_btn")
                            ) {
                                Text("بعداً")
                            }
                        }
                    }
                }
            }

            // First launch guide card (auto-dismissed forever when first farm created)
            if (settings?.guideDismissed == false && farms.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "اول یک مزرعه بساز ← بعد اشخاص را اضافه کن",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tile layout header & Customize button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بخش‌های اصلی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { isEditLayoutMode = !isEditLayoutMode },
                        modifier = Modifier.testTag("customize_layout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DashboardCustomize,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditLayoutMode) "اتمام چیدمان" else "چیدمان")
                    }
                }
            }

            // Tiles List
            val visibleKeys = savedOrder.filter { isEditLayoutMode || !hiddenTiles.contains(it) }

            items(visibleKeys, key = { it }) { key ->
                val tile = tileInfoMap[key]
                if (tile != null) {
                    val isHidden = hiddenTiles.contains(key)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isEditLayoutMode && !isHidden) {
                                viewModel.navigateTo(tile.screen)
                            }
                            .testTag("tile_${tile.key}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHidden) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tile.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHidden) "پنهان شده" else tile.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Layout editor controls
                            if (isEditLayoutMode) {
                                val index = savedOrder.indexOf(key)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Move Up
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val mutable = savedOrder.toMutableList()
                                                val temp = mutable[index]
                                                mutable[index] = mutable[index - 1]
                                                mutable[index - 1] = temp
                                                viewModel.updateTileLayout(mutable, hiddenTiles.toList())
                                            }
                                        },
                                        enabled = index > 0
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "انتقال به بالا")
                                    }
                                    // Move Down
                                    IconButton(
                                        onClick = {
                                            if (index < savedOrder.size - 1) {
                                                val mutable = savedOrder.toMutableList()
                                                val temp = mutable[index]
                                                mutable[index] = mutable[index + 1]
                                                mutable[index + 1] = temp
                                                viewModel.updateTileLayout(mutable, hiddenTiles.toList())
                                            }
                                        },
                                        enabled = index < savedOrder.size - 1
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "انتقال به پایین")
                                    }
                                    // Toggle Visibility
                                    IconButton(
                                        onClick = {
                                            val newHidden = hiddenTiles.toMutableSet()
                                            if (isHidden) newHidden.remove(key) else newHidden.add(key)
                                            viewModel.updateTileLayout(savedOrder, newHidden.toList())
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "تغییر نمایش",
                                            tint = if (isHidden) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
