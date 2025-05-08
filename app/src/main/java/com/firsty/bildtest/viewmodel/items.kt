package com.firsty.bildtest.viewmodel

import com.firsty.bildtest.R

data class Item(val id: Int, val text: String, val size: Int, val imageResId: Int)

val items = (0..200).map {
    Item(
        id = it,
        text = "Item #$it",
        size = if (it % 2 == 0) 70 else 100,
        imageResId = if (it % 2 == 0) {
            R.drawable.cat
        } else {
            println("Error: Kein gültiger Wert für imageResId gefunden")
            throw IllegalArgumentException("Ungültiger Wert für imageResId")
        }
    )
}
