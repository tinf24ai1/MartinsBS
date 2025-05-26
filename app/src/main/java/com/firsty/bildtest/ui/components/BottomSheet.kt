package com.firsty.bildtest.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.firsty.bildtest.viewmodel.ImageViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    imageViewModel: ImageViewModel,
    sheetState: SheetState,
    onClose: () -> Unit,
    onAddImagesClick: () -> Unit
) {
    val imageList = imageViewModel.imageList
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var sliderValue by remember { mutableStateOf(1.0f) }

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
                    imageViewModel.removeImages(imageList)
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

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeight * 2f) / 3f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                            imageViewModel.removeImages(selectedForDeletion)
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
                            if (imageList.isNotEmpty()) {
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(imageList.size) { index ->
                            val imageUri = imageList[index]
                            val isSelected = selectedForDeletion.contains(imageUri)

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
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
                                    contentDescription = "Selected image",
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
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Slideshow Speed: ${String.format("%.1f", sliderValue)} sec")
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        // Optional: apply to slideshow timing
                    },
                    valueRange = 0.5f..5f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
