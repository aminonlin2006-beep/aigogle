package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PALETTE_12

@Composable
fun ColorPalettePicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("انتخاب رنگ برچسب:", style = MaterialTheme.typography.labelLarge)

        // 2 rows of 6 colors
        val row1 = PALETTE_12.take(6)
        val row2 = PALETTE_12.drop(6)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row1.forEach { colorHex ->
                ColorCircle(
                    colorHex = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row2.forEach { colorHex ->
                ColorCircle(
                    colorHex = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
    colorHex: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(colorHex)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "انتخاب شده",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
