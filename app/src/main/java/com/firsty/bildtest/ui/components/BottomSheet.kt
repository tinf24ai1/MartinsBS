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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.firsty.bildtest.viewmodel.ImageViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    imageViewModel: ImageViewModel,
    sheetState: SheetState,
    onClose: () -> Unit
) {

    val imageList = imageViewModel.imageList
    var showHelpDialog by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Filled.Help,
                        contentDescription = "Hilfe",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showHelpDialog = true } // Hier klickbar machen
                    )


                }

                Text(
                    text = "Selected Images",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val imagesToShow: List<Int> = imageList.take(8)
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
                            .height((100.dp * 2))
                            .padding(10.dp),
                        userScrollEnabled = false
                    ) {
                        items(imagesToShow.size) { index ->
                            val imageResId = imagesToShow[index]
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = "Image $index",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

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
