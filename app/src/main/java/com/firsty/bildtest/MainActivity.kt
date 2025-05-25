package com.firsty.bildtest

import android.annotation.SuppressLint
import android.content.Intent
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
import com.firsty.bildtest.core.services.UnlockReceiverService

// TODO: Check and fix errors and warnings in Logcat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "starting app with PID " + android.os.Process.myPid())

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

        // Berechtigungen von User anfordern, wenn nicht schon erteilt
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        // Wenn der UnlockReceiverService zum Öffnen der App nicht läuft, dann starten
        startUnlockService()

    }

    /**
     * Berechtigungs-Launcher konfigurieren
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted:Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Permission granted")
        } else {
            // TODO: Popup anzeigen, dass die Berechtigung benötigt wird, damit die APP Autostarten kann
            Log.d("MainActivity", "Permission denied")
        }
    }

    /**
     * Berechtigungen für "Nachrichten anzeigen" anfordern
     */
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

    /**
     * Startet den UnlockReceiverService. Wenn der Service bereits läuft wird dieser überprüfen,
     * ob die Notification angezeigt wird, und wenn nicht, zeigt sie an.
     */
    private fun startUnlockService() {
        Log.d("MainActivity", "Trying to start UnlockReceiverService...")
        val intent = Intent(this, UnlockReceiverService::class.java)
        ContextCompat.startForegroundService(this, intent)
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