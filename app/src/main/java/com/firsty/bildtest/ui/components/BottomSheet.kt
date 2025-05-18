package com.firsty.bildtest.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.*
import com.firsty.bildtest.ui.haptics.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.firsty.bildtest.viewmodel.*
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.ui.zIndex


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    sheetState: SheetState,
    onClose: () -> Unit
) {
    val imageList = items

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            DragHandle()
        }
    ) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        var sliderValue by remember { mutableFloatStateOf(1.0f) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeight * 2f) / 3f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(25.dp)
                    )
                }

                Text(
                    text = "Selected Images",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // <--- Begin Picture-Grid --->
                val haptic = rememberReorderHapticFeedback()
                var list by remember { mutableStateOf(imageList) }
                val lazyGridState = rememberLazyGridState()
                val reorderableLazyGridState =
                    rememberReorderableLazyGridState(lazyGridState) { from, to ->
                        list = list.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        }
                        haptic.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
                    }

                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100.dp * 2))
                        .padding(10.dp),
                    userScrollEnabled = true
                ) {
                    itemsIndexed(list, key = { _, item -> item.id }) { index, item ->
                        ReorderableItem(reorderableLazyGridState, item.id) {
                            val interactionSource = remember { MutableInteractionSource() }
                            Card(
                                onClick = {},
                                modifier = Modifier
                                    .height(96.dp)
                                    .semantics {
                                        customActions = listOf(
                                            CustomAccessibilityAction(
                                                label = "Move Before",
                                                action = {
                                                    if (index > 0) {
                                                        list = list.toMutableList().apply {
                                                            add(index - 1, removeAt(index))
                                                        }
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                }
                                            ),
                                            CustomAccessibilityAction(
                                                label = "Move After",
                                                action = {
                                                    if (index < list.size - 1) {
                                                        list = list.toMutableList().apply {
                                                            add(index + 1, removeAt(index))
                                                        }
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                }
                                            ),
                                        )
                                    },
                                interactionSource = interactionSource,
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = item.id),
                                        contentDescription = item.text,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .zIndex(1f)
                                            .draggableHandle(
                                                onDragStarted = {
                                                    haptic.performHapticFeedback(
                                                        ReorderHapticFeedbackType.START
                                                    )
                                                },
                                                onDragStopped = {
                                                    haptic.performHapticFeedback(
                                                        ReorderHapticFeedbackType.END
                                                    )
                                                },
                                                interactionSource = interactionSource,
                                            )
                                            .clearAndSetSemantics { },
                                        onClick = {},
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DragHandle,
                                            contentDescription = "Drag Handle",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // <--- End Picture-Grid --->


                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Animation Speed",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${String.format("%.1f", sliderValue)}x",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Slider(
                    value = sliderValue,
                    onValueChange = { newValue -> sliderValue = newValue },
                    valueRange = 0.5f..3.0f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
