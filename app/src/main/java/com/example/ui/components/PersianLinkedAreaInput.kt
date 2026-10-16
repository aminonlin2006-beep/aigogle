package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.util.PersianNumberFormatter

/**
 * Linked area input component:
 * 1 hectare = 10,000 square meters.
 * Typing in one auto-calculates the other in real time.
 */
@Composable
fun PersianLinkedAreaInput(
    initialAreaM2: Double?,
    onAreaM2Changed: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var haText by remember {
        mutableStateOf(
            if (initialAreaM2 != null && initialAreaM2 > 0) {
                PersianNumberFormatter.formatDecimal(initialAreaM2 / 10000.0, 3)
            } else ""
        )
    }

    var sqmText by remember {
        mutableStateOf(
            if (initialAreaM2 != null && initialAreaM2 > 0) {
                PersianNumberFormatter.formatDecimal(initialAreaM2, 1)
            } else ""
        )
    }

    var lastEditedField by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialAreaM2) {
        if (lastEditedField == null) {
            if (initialAreaM2 != null && initialAreaM2 > 0) {
                haText = PersianNumberFormatter.formatDecimal(initialAreaM2 / 10000.0, 3)
                sqmText = PersianNumberFormatter.formatDecimal(initialAreaM2, 1)
            } else {
                haText = ""
                sqmText = ""
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hectares Input
            OutlinedTextField(
                value = haText,
                onValueChange = { input ->
                    lastEditedField = "ha"
                    haText = input
                    val haValue = PersianNumberFormatter.parseToDouble(input)
                    if (haValue != null && haValue >= 0) {
                        val sqm = haValue * 10000.0
                        sqmText = PersianNumberFormatter.formatDecimal(sqm, 1)
                        onAreaM2Changed(sqm)
                    } else {
                        sqmText = ""
                        onAreaM2Changed(null)
                    }
                },
                label = { Text("هکتار") },
                placeholder = { Text("مثلاً ۲٫۵") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            // Square Meters Input
            OutlinedTextField(
                value = sqmText,
                onValueChange = { input ->
                    lastEditedField = "sqm"
                    sqmText = input
                    val sqmValue = PersianNumberFormatter.parseToDouble(input)
                    if (sqmValue != null && sqmValue >= 0) {
                        val ha = sqmValue / 10000.0
                        haText = PersianNumberFormatter.formatDecimal(ha, 3)
                        onAreaM2Changed(sqmValue)
                    } else {
                        haText = ""
                        onAreaM2Changed(null)
                    }
                },
                label = { Text("متر مربع") },
                placeholder = { Text("مثلاً ۲۵٬۰۰۰") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
