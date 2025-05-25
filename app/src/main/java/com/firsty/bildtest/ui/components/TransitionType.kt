package com.firsty.bildtest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TransitionType(
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    icons: List<ImageVector>,
    labels: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Transition Type",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icons.forEachIndexed { index, icon ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Button with Icon
                    IconButton(
                        onClick = { onSelectedChange(index) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (selectedIndex == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = labels[index],
                            tint = if (selectedIndex == index)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Subtext
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
