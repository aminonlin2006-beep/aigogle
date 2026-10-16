package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AgriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AgriViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val deviceSettings by viewModel.deviceSettings.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val enabledWorkTypes = remember(settings?.enabledWorkTypesJson) {
        viewModel.parseJsonStringList(settings?.enabledWorkTypesJson ?: "")
    }

    val isFarmingEnabled = enabledWorkTypes.contains("farming")
    val isNurseryEnabled = enabledWorkTypes.contains("nursery")

    val fontSize = deviceSettings?.fontSize ?: "normal"
    val currentTheme = deviceSettings?.theme ?: "light"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات", fontWeight = FontWeight.Bold) },
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

            // Work Types Card (Section 4-7, Decisions 6, 161)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "نوع‌های کار فعال در اپ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "اگر فقط یکی از دو مدل زراعت یا نهالستان را دارید، می‌توانید دیگری را خاموش کنید تا در فرم‌ها پرسیده نشود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Farming toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("زراعت (غلات، صیفی و باغات)", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = isFarmingEnabled,
                                onCheckedChange = { viewModel.toggleWorkType("farming") },
                                modifier = Modifier.testTag("farming_switch")
                            )
                        }

                        // Nursery toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("نهالستان (تولید و پرورش نهال و گلدان)", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = isNurseryEnabled,
                                onCheckedChange = { viewModel.toggleWorkType("nursery") },
                                modifier = Modifier.testTag("nursery_switch")
                            )
                        }
                    }
                }
            }

            // Work Mode Card (Decision 2: local only)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "حالت کار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = true,
                                onClick = {}
                            )
                            Column {
                                Text("فقط روی گوشی (آفلاین)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "داده‌ها فقط روی همین گوشی ذخیره می‌شوند و نیاز به اینترنت نیست.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Font Size Card (Decisions 132, 134)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "اندازه نوشته",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.setFontSize("normal") }
                                    .testTag("font_normal_radio")
                            ) {
                                RadioButton(
                                    selected = fontSize == "normal",
                                    onClick = { viewModel.setFontSize("normal") }
                                )
                                Text("معمولی (پیش‌فرض)")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.setFontSize("large") }
                                    .testTag("font_large_radio")
                            ) {
                                RadioButton(
                                    selected = fontSize == "large",
                                    onClick = { viewModel.setFontSize("large") }
                                )
                                Text("بزرگ")
                            }
                        }
                    }
                }
            }

            // Theme Card (Decision 135)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "پوسته ظاهری",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.setTheme("light") }
                                    .testTag("theme_light_radio")
                            ) {
                                RadioButton(
                                    selected = currentTheme == "light",
                                    onClick = { viewModel.setTheme("light") }
                                )
                                Text("روشن (پیش‌فرض)")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.setTheme("dark") }
                                    .testTag("theme_dark_radio")
                            ) {
                                RadioButton(
                                    selected = currentTheme == "dark",
                                    onClick = { viewModel.setTheme("dark") }
                                )
                                Text("تیره")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
