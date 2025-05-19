package com.firsty.bildtest.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.firsty.bildtest.R

data class Item(val id: Int, val text: String, val size: Int)

class ImageViewModel : ViewModel() {

    private var _items by mutableStateOf(
        listOf(
            Item(id = R.drawable.cat, text = "Katze 1", size = 100),
            Item(id = R.drawable.cat2, text = "Katze 2", size = 100),
            Item(id = R.drawable.cat3, text = "Katze 3", size = 100)
        )
    )


    val items: List<Item> get() = _items

    fun updateItems(newList: List<Item>) {
        _items = newList
    }
}