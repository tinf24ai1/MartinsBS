package com.firsty.bildtest

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.offset
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.rememberAsyncImagePainter


// Package Intern References
import com.firsty.bildtest.ui.components.BottomSheet
import com.firsty.bildtest.ui.theme.BildTestTheme
import com.firsty.bildtest.viewmodel.ImageViewModel
import com.firsty.bildtest.core.services.UnlockReceiverService

import kotlin.text.get
import kotlin.times

class MainActivity : ComponentActivity() {
    private val imageViewModel: ImageViewModel by viewModels()
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
        imageViewModel.loadItems(this)

        setContent {
            BildTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Slideshow(viewModel(), pickImagesLauncher)
                }
            }
        }

        // Berechtigungen von User anfordern, wenn nicht schon erteilt
        requestNotificationPermission()
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
            imageViewModel.saveItems(this)
        }
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
fun Slideshow(viewModel: ImageViewModel = viewModel(), pickImagesLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
    // using images from items.kt
    val list = viewModel.items

    // keeping track of current image index
    var currentIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // manage state of bottom sheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // state of the bottom sheets visibility
    var showSheet by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    var previousIndex by remember { mutableIntStateOf(0) }

    // Auto-switch images every 5 seconds
    LaunchedEffect(viewModel.cycleInterval) {
        while (true) {
            delay(viewModel.cycleInterval * 1000L)

            if (list.isNotEmpty()) {
                previousIndex = currentIndex
                currentIndex = (currentIndex + 1) % list.size
            }
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
        // depending on the selected transition type
        if(list.isNotEmpty()){
            when (viewModel.transitionType) {
                0 -> { // Instantly
                    Image(
                        painter = rememberAsyncImagePainter(list[currentIndex].uri),
                        contentDescription = list[currentIndex].text,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                1 -> { // Fade
                    Crossfade(
                        targetState = currentIndex,
                        animationSpec = tween(durationMillis = 3000, easing = LinearOutSlowInEasing)
                    ) { index ->
                        Image(
                            painter = rememberAsyncImagePainter(list[index].uri),
                            contentDescription = list[index].text,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                }


                2 -> { // Slide from left
                    val slideProgress = remember(currentIndex) { Animatable(-1f) }

                    var visibleNewImage by remember { mutableStateOf(false) }

                    LaunchedEffect(currentIndex) {
                        visibleNewImage = true
                        slideProgress.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }


                    val screenWidthPx = with(density) {
                        LocalConfiguration.current.screenWidthDp.dp.toPx()
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                    ) {
                        if (slideProgress.value < 1f) {
                            // Old image: starts at 0, moves right
                            Image(
                                painter = rememberAsyncImagePainter(list[previousIndex].uri),
                                contentDescription = list[previousIndex].text,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            (slideProgress.value * screenWidthPx).roundToInt(),
                                            0
                                        )
                                    }

                            )
                        }

                        if (visibleNewImage) {
                            // New image: starts left, moves to center
                            Image(
                                painter = rememberAsyncImagePainter(list[currentIndex].uri),
                                contentDescription = list[currentIndex].text,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            ((slideProgress.value - 1) * screenWidthPx).roundToInt(),
                                            0
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }


    // show bottom sheet
    if (showSheet) {
        BottomSheet(viewModel= viewModel, sheetState = sheetState, onClose = {
            showSheet = false
            scope.launch { sheetState.hide() }
        },
                onAddImagesClick = {
            // ✅ Launch with OpenMultipleDocuments
            pickImagesLauncher.launch(arrayOf("image/*"))
        })
    }
}