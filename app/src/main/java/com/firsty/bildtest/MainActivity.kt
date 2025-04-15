package com.firsty.bildtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.firsty.bildtest.ui.theme.BildTestTheme
import com.firsty.bildtest.R
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun Slideshow() {
    // List of image resource IDs
    val imageList = listOf(
        R.drawable.cat,
        R.drawable.cat2,
        R.drawable.cat3
    )

    var currentIndex by remember { mutableStateOf(0) }

    // Auto-switch image every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            currentIndex = (currentIndex + 1) % imageList.size
        }
    }

    Image(
        painter = painterResource(id = imageList[currentIndex]),
        contentDescription = "Slideshow image",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun SlideshowPreview() {
    BildTestTheme {
        Slideshow()
    }
}
