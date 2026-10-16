package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.entities.SnapshotEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.viewmodel.AgriViewModel
import com.example.util.PersianDateUtils
import com.example.util.PersianNumberFormatter
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val meta by viewModel.meta.collectAsState()
    val snapshots by viewModel.snapshots.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val unsentCount = meta?.unsentChanges ?: 0

    var showDidSendDialog by remember { mutableStateOf(false) }
    var showRestoreWarningDialog by remember { mutableStateOf(false) }
    var restoreJsonPending by remember { mutableStateOf<String?>(null) }
    var isSnapshotRestorePending by remember { mutableStateOf(false) }

    var showPasteFileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonString = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (!jsonString.isNullOrBlank()) {
                    restoreJsonPending = jsonString
                    isSnapshotRestorePending = false
                    showRestoreWarningDialog = true
                }
            } catch (e: Exception) {
                viewModel.showMessage("خطا در خواندن فایل انتخاب شده.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پشتیبان‌گیری و بازگردانی", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            // Unsent Changes Status Card (Section 4-8)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unsentCount == 0) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (unsentCount == 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (unsentCount == 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (unsentCount == 0) "همه تغییرات پشتیبان‌گیری شده‌اند"
                                else "${PersianNumberFormatter.formatInteger(unsentCount.toLong())} تغییر ارسال‌نشده",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (unsentCount == 0) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (unsentCount == 0) "وضعیت پشتیبان شما کاملاً سبز و امن است."
                                else "تغییراتی در مزارع، اشخاص یا برچسب‌ها داده‌اید که هنوز فایل پشتیبان آن را به بیرون نفرستاده‌اید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (unsentCount == 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Export Backup Button (Section 4-8)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ارسال فایل پشتیبان به بیرون", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "فایل پشتیبان کامل به نام «پشتیبان-مزرعه-${PersianDateUtils.today().toBackupDateString()}.json» آماده می‌شود و می‌توانید آن را در تلگرام، ایتا، ایمیل یا پیام‌رسان دلخواه برای خودتان بفرستید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    exportAndShareBackup(context, viewModel) {
                                        showDidSendDialog = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_backup_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("فرستادن فایل پشتیبان")
                        }
                    }
                }
            }

            // Restore Backup from File (Section 4-8)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("برگرداندن از فایل پشتیبان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "با برگرداندن فایل، کل اطلاعات موجود جایگزین خواهد شد. اپ به‌طور خودکار قبل از جایگزینی یک نسخه از اطلاعات فعلی ذخیره می‌کند.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch("application/json")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("select_backup_file_btn")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("انتخاب فایل")
                            }
                            OutlinedButton(
                                onClick = { showPasteFileDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("paste_backup_text_btn")
                            ) {
                                Text("ورود متن فایل")
                            }
                        }
                    }
                }
            }

            // Automatic Daily & Restore Snapshots (Section 4-8, Decisions 136, 162, پ11, پ12)
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نسخه‌های خودکار داخل اپ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (snapshots.isEmpty()) {
                item {
                    Text(
                        text = "هنوز نسخه خودکاری ثبت نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(snapshots, key = { it.id }) { snapshot ->
                    val kindLabel = if (snapshot.kind == "beforeRestore") "قبل از برگرداندن فایل" else "نسخه روزانه"
                    val formattedDate = PersianDateUtils.formatDateTime(snapshot.createdAt)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                restoreJsonPending = snapshot.data
                                isSnapshotRestorePending = true
                                showRestoreWarningDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                    text = kindLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (snapshot.kind == "beforeRestore") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    restoreJsonPending = snapshot.data
                                    isSnapshotRestorePending = true
                                    showRestoreWarningDialog = true
                                }
                            ) {
                                Text("بازگردانی")
                            }
                        }
                    }
                }
            }

            // Advisory Note (Decision 140, Section 4-8)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تنها پشتیبان مطمئن، فایلی است که بیرون فرستاده‌ای.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    // "فرستادی؟" Dialog (Decision 138)
    if (showDidSendDialog) {
        AlertDialog(
            onDismissRequest = { showDidSendDialog = false },
            title = { Text("فرستادی؟", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "آیا فایل پشتیبان را با موفقیت در پیام‌رسان یا جای مطمئن ارسال کردید؟ با تأیید شما، علامت پشتیبان سبز خواهد شد.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmBackupSent()
                        showDidSendDialog = false
                    },
                    modifier = Modifier.testTag("confirm_sent_yes")
                ) {
                    Text("بله، فرستادم")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDidSendDialog = false },
                    modifier = Modifier.testTag("confirm_sent_no")
                ) {
                    Text("هنوز نه")
                }
            }
        )
    }

    // Strong Restore Warning Dialog (Decision 139)
    if (showRestoreWarningDialog && restoreJsonPending != null) {
        ConfirmDialog(
            title = "هشدار بازگردانی پشتیبان",
            message = "همه‌ی داده‌ی فعلی با این فایل عوض می‌شود. آیا مطمئنید؟",
            confirmText = "تأیید و جایگزینی کل داده",
            isDestructive = true,
            onConfirm = {
                val json = restoreJsonPending ?: return@ConfirmDialog
                viewModel.restoreBackup(json, isInternalSnapshot = isSnapshotRestorePending) { success ->
                    showRestoreWarningDialog = false
                    restoreJsonPending = null
                }
            },
            onDismiss = {
                showRestoreWarningDialog = false
                restoreJsonPending = null
            }
        )
    }

    // Paste Backup JSON Dialog (Alternative for manual import / testing)
    if (showPasteFileDialog) {
        var pastedText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasteFileDialog = false },
            title = { Text("ورود متن فایل پشتیبان") },
            text = {
                Column {
                    Text("متن محتوای فایل پشتیبان (JSON) را اینجا وارد کنید:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("محتوای JSON...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedText.isNotBlank()) {
                            restoreJsonPending = pastedText.trim()
                            isSnapshotRestorePending = false
                            showPasteFileDialog = false
                            showRestoreWarningDialog = true
                        }
                    }
                ) {
                    Text("بررسی و بازگردانی")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasteFileDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

/**
 * Creates temporary JSON backup file and opens Android Share Sheet
 */
private suspend fun exportAndShareBackup(
    context: Context,
    viewModel: AgriViewModel,
    onShared: () -> Unit
) {
    try {
        val jsonString = viewModel.getBackupJson()
        val jalaliDate = PersianDateUtils.today().toBackupDateString()
        // Format: پشتیبان-مزرعه-۱۴۰۵-۰۷-۲۴.json (Decision 141)
        val fileName = "پشتیبان-مزرعه-$jalaliDate.json"

        val cacheFile = File(context.cacheDir, fileName)
        cacheFile.writeText(jsonString)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, jsonString)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            type = "application/json"
        }
        val shareIntent = Intent.createChooser(sendIntent, "ارسال فایل پشتیبان به...")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)

        onShared()
    } catch (e: Exception) {
        viewModel.showMessage("خطا در ایجاد فایل پشتیبان: ${e.message}")
    }
}
