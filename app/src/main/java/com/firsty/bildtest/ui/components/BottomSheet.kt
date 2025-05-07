package com.firsty.bildtest.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.firsty.bildtest.viewmodel.ImageViewModel
import sh.calvin.reorderable.*
import android.util.Log
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import kotlin.text.get


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    imageViewModel: ImageViewModel,
    sheetState: SheetState,
    onClose: () -> Unit
) {

    val imageList = imageViewModel.imageList

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

                // Begin Picture-Grid

                val imagesToShow: List<Int> = imageList.take(8)
//                if (imagesToShow.isNotEmpty()) {
//                    val context = LocalContext.current
//                    Log.d("BottomSheet", "Resource Name: ${context.resources.getResourceName(imagesToShow[0])}")
//                }

                    val context = LocalContext.current

                val lazyGridState = rememberLazyGridState()
                val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
                    imageViewModel.updateImageListAfterDragAndDrop(from.index, to.index)
                }

                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100.dp * 2))
                        .padding(10.dp),
                    userScrollEnabled = true // Scrollen aktiviert
                ) {
                    items(imagesToShow.size, key = { imagesToShow[it] }) { index ->
                        val imageResId = imagesToShow[index]
                        ReorderableItem(
                            reorderableLazyGridState,
                            key = imageResId
                        ) { isDragging ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = imageResId),
                                    contentDescription = "Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    modifier = Modifier
                                        .draggableHandle()
                                        .align(Alignment.TopEnd), // Positionierung des Drag-Handles
                                    onClick = {
                                        Log.d("IconButton", "Button wurde geklickt")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Drag Handle"
                                    )
                                }
                            }
                        }
                    }
                }
                    // End Picture-Grid

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
