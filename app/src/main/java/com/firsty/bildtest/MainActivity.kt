package com.firsty.bildtest

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Package Intern References
import com.firsty.bildtest.ui.components.BottomSheet
import com.firsty.bildtest.ui.theme.BildTestTheme
import com.firsty.bildtest.viewmodel.items

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App in Fullscreen anzeigen
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Verhalten der versteckten System-Leisten einstellen
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // System-Leisten verstecken
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            BildTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Slideshow()
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Slideshow() {
    // using images from items.kt
    val imageList = items

    // keeping track of current image index
    var currentIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // manage state of bottom sheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // state of the bottom sheets visibility
    var showSheet by remember { mutableStateOf(false) }

    // Auto-switch images every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            currentIndex = (currentIndex + 1) % imageList.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // detect vertical drag gestures to show the bottom sheet
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) {
                        showSheet = true
                        scope.launch {
                            sheetState.show()
                        }
                    }
                }
            }
    ) {
        // displayed image
        Image(
            // show image at current index
            painter = painterResource(id = imageList[currentIndex].id),
            // content description for accessibility
            contentDescription = imageList[currentIndex].text,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // show bottom sheet
        if (showSheet) {
            BottomSheet(sheetState = sheetState, onClose = {
                showSheet = false
                scope.launch { sheetState.hide() }
            })
        }
    }
}