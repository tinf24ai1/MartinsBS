package com.firsty.bildtest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IntervalSlider(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit
) {
    val intervals = listOf(3, 5, 10, 15, 20, 30, 60, 300, 600, 1800, 3600, 18000, 86400) // Sekunden
    val initialIndex = intervals.indexOf(currentInterval).coerceAtLeast(0)
    var selectedIndex by remember { mutableStateOf(initialIndex) }
    val selectedInterval = intervals[selectedIndex]

    // Wenn sich selectedInterval ändert, gib ihn an den Aufrufer zurück
    LaunchedEffect(selectedInterval) {
        onIntervalChange(selectedInterval)
    }

    Column {
        Text(
            text = "Image Change Interval",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = when (selectedInterval) {
                in 60..299 -> "${selectedInterval / 60} min"
                300 -> "5 min"
                600 -> "10 min"
                1800 -> "30 min"
                3600 -> "1 h"
                18000 -> "5 h"
                86400 -> "24 h"

                else -> "$selectedInterval s"
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { selectedIndex = it.toInt() },
            valueRange = 0f..(intervals.size - 1).toFloat(),
            steps = intervals.size - 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
