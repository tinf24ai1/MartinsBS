package com.firsty.bildtest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Package Intern References
import com.firsty.bildtest.ui.components.BottomSheet
import com.firsty.bildtest.ui.theme.BildTestTheme
import com.firsty.bildtest.viewmodel.ImageViewModel

// TODO: App crashes on automatic startup (BildTest keeps stopping)
// TODO: Notification isn't displayed, even though manually enabled
// TODO: NotificationManager isn't displayed
// TODO: Check and fix errors and warnings in Logcat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "starting app with PID " + android.os.Process.myPid())

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

        // Berechtigungen von User anfordern
        requestNotificationPermission()
    }

    // Berechtigungs-Launcher konfigurieren
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted:Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Permission granted")
        } else {
            // TODO: Popup anzeigen, dass die Berechtigung benötigt wird, damit die APP autostarten kann
            Log.d("MainActivity", "Permission denied")
        }
    }

    // Berechtigungen für "Nachrichten anzeigen" anfordern
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Slideshow(imageViewModel: ImageViewModel = ImageViewModel()) {
    val imageList = imageViewModel.imageList

    var currentIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) { // Swipe up
                        showSheet = true
                        scope.launch {
                            sheetState.show()
                        }
                    }
                }
            }
    ) {
        Image(
            painter = painterResource(id = imageList[currentIndex]),
            contentDescription = "Slideshow image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        if (showSheet) {
            BottomSheet(imageViewModel, sheetState) {
                showSheet = false
                scope.launch { sheetState.hide() }
            }
        }
    }
}