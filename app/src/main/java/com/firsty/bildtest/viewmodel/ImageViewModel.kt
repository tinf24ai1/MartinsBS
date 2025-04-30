package com.firsty.bildtest.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.firsty.bildtest.R

class ImageViewModel : ViewModel() {

    private var _imageList by mutableStateOf(
        listOf(
            R.drawable.cat,
            R.drawable.cat2,
            R.drawable.cat3,
        )
    )

    val imageList: List<Int> get() = _imageList

    fun updateImageList(newList: List<Int>) {
        _imageList = newList
    }
    
}
