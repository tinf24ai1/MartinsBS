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
import androidx.compose.material.icons.filled.Help
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
import com.firsty.bildtest.viewmodel.ImageViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import sh.calvin.reorderable.*
import com.firsty.bildtest.ui.haptics.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import com.firsty.bildtest.ui.components.IntervalSlider
import com.firsty.bildtest.ui.components.TransitionType
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import coil.compose.rememberAsyncImagePainter

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    viewModel: ImageViewModel,
    sheetState: SheetState,
    onClose: () -> Unit,
    onAddImagesClick: () -> Unit
) {
    val context = LocalContext.current
    val list = viewModel.items
    var isDeleteMode by remember { mutableStateOf(false) }
    val selectedForDeletion = remember { mutableStateListOf<android.net.Uri>() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(text = "Delete All Images?") },
            text = { Text("Are you sure you want to delete ALL images? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {

                    viewModel.removeImages(list,context)
                    selectedForDeletion.clear()
                    isDeleteMode = false
                    showDeleteAllDialog = false
                }) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    var showHelpDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            DragHandle()
        }
    ) {
        // screen height for dynamic height
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeight * 2f) / 3f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // header for bottom sheet
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
                        imageVector = Icons.Filled.Help,
                        contentDescription = "Hilfe",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showHelpDialog = true } // Hier klickbar machen
                    )


                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = if (isDeleteMode) "Tap images to delete" else "Selected Images",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    if (isDeleteMode) {
                        IconButton(onClick = {
                            selectedForDeletion.clear()
                            isDeleteMode = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = {
                            val itemsToDelete = list.filter { selectedForDeletion.contains(it.uri) }
                            viewModel.removeImages(itemsToDelete, context)
                            selectedForDeletion.clear()
                            isDeleteMode = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm Delete",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        IconButton(onClick = onAddImagesClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                        IconButton(onClick = {
                            isDeleteMode = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Images",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                        IconButton(onClick = {
                            if (list.isNotEmpty()) {
                                showDeleteAllDialog = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Delete All Images",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }


                // image grid with reorderable items
                val haptic = rememberReorderHapticFeedback()


                val lazyGridState = rememberLazyGridState()
                // logic for reordering items
                val reorderableLazyGridState =
                    rememberReorderableLazyGridState(lazyGridState) { from, to ->
                        viewModel.updateItems(list.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        },context)
                        haptic.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
                    }



                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(10.dp),
                    userScrollEnabled = true
                ) {
                    itemsIndexed(list, key = { _, item -> item.id }) { index, item ->
                        val imageUri = item.uri // or however your list is structured
                        val isSelected = selectedForDeletion.contains(imageUri)

                        ReorderableItem(reorderableLazyGridState, item.id) { isDragging ->
                            val interactionSource = remember { MutableInteractionSource() }

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(80.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                                    .then(
                                        if (!isDeleteMode)
                                            Modifier.shadow(3.dp, shape = MaterialTheme.shapes.medium)
                                        else Modifier
                                    )
                                    .clickable(enabled = isDeleteMode) {
                                        if (isSelected) {
                                            selectedForDeletion.remove(imageUri)
                                        } else {
                                            selectedForDeletion.add(imageUri)
                                        }
                                    }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = item.text,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )

                                if (isDeleteMode && isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.Black.copy(alpha = 0.4f))
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(20.dp)
                                    )
                                }

                                if (!isDeleteMode) {
                                    IconButton(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .zIndex(1f)
                                            .draggableHandle(
                                                onDragStarted = {
                                                    haptic.performHapticFeedback(ReorderHapticFeedbackType.START)
                                                },
                                                onDragStopped = {
                                                    haptic.performHapticFeedback(ReorderHapticFeedbackType.END)
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
                                            modifier = Modifier.size(25.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }



                // gap below grid
                Spacer(modifier = Modifier.height(32.dp))

                //Interval slider logic in IntervaSlider.kt
                IntervalSlider(
                    currentInterval = viewModel.cycleInterval,
                    onIntervalChange = { viewModel.cycleInterval = it }
                )

                // transition type selection
                val selectedTransition = viewModel.transitionType


                TransitionType(
                    selectedIndex = selectedTransition,
                    onSelectedChange = { viewModel.transitionType = it },
                    icons = listOf(
                        Icons.Filled.Slideshow,     // instant
                        Icons.Filled.Opacity,       // fade
                        Icons.Filled.ViewCarousel   // slide
                    ),
                    labels = listOf("Instant", "Fade", "Slide")
                )

                val context = LocalContext.current // Move this inside the lambda
                if (showHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showHelpDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {

                                    val pdfIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dein-link-zur-anleitung.de/anleitung.pdf"))
                                    context.startActivity(pdfIntent)
                                    showHelpDialog = false
                                }
                            ) {
                                Text("Anleitung öffnen")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showHelpDialog = false }) {
                                Text("Schließen")
                            }
                        },
                        title = { Text("Need Help?") },
                        text = {
                            Text("Hier können Sie die Benutzeranleitung herunterladen.")
                        }
                    )
                }
            }
        }
    }
}
