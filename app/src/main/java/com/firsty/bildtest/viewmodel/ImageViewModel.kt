package com.firsty.bildtest.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModel(private val context: Context) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE)

    // Keep this mutableStateListOf to trigger Compose recompositions on changes
    private val _imageList = mutableStateListOf<Uri>()
    val imageList = _imageList

    init {
        loadSavedUris()
    }

    private fun loadSavedUris() {
        val savedUris = prefs.getStringSet("saved_image_uris", emptySet())
        savedUris?.let {
            val uris = it.mapNotNull { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    // 👇 Request persisted permissions again
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    uri
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            _imageList.addAll(uris)
        }
    }

    private fun saveUris() {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit().putStringSet("saved_image_uris", _imageList.map { it.toString() }.toSet()).apply()
        }
    }

    fun addImagesFromUris(uris: List<Uri>) {
        val newUris = uris.filter { it !in _imageList }
        _imageList.addAll(newUris)
        saveUris()
    }
}

// Factory to create ViewModel with Context parameter
class ImageViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
