package com.firsty.bildtest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.rememberAsyncImagePainter
import com.firsty.bildtest.ui.components.BottomSheet
import com.firsty.bildtest.ui.theme.BildTestTheme
import com.firsty.bildtest.viewmodel.ImageViewModel
import com.firsty.bildtest.viewmodel.ImageViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val imageViewModel: ImageViewModel by viewModels {
        ImageViewModelFactory(applicationContext)
    }

    // ✅ Correct contract for persistable permission
    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
            imageViewModel.addImagesFromUris(uris)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen setup (hide system bars, swipe to show)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            BildTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Slideshow(imageViewModel, pickImagesLauncher)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Slideshow(
    imageViewModel: ImageViewModel,
    pickImagesLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val imageList = imageViewModel.imageList

    var currentIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSheet by remember { mutableStateOf(false) }

    // Auto-switch images every 5 seconds
    LaunchedEffect(imageList) {
        if (imageList.isNotEmpty()) {
            while (true) {
                delay(5000L)
                currentIndex = (currentIndex + 1) % imageList.size
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(sheetState.isVisible) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50 && !sheetState.isVisible) { // swipe up to show sheet
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }
                }
            }
    ) {
        if (imageList.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageList[currentIndex]),
                contentDescription = "Slideshow image",
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showSheet) {
            BottomSheet(
                imageViewModel = imageViewModel,
                sheetState = sheetState,
                onClose = {
                    showSheet = false
                    scope.launch { sheetState.hide() }
                },
                onAddImagesClick = {
                    // ✅ Launch with OpenMultipleDocuments
                    pickImagesLauncher.launch(arrayOf("image/*"))
                }
            )
        }
    }

    // Show sheet on app start
    LaunchedEffect(Unit) {
        showSheet = true
        scope.launch { sheetState.show() }
    }
}
