package com.firsty.bildtest.viewmodel

import com.firsty.bildtest.R

data class Item(val id: Int, val text: String, val size: Int)

val items = listOf(
    Item(id = R.drawable.cat, text = "Katze 1", size = 100),
    Item(id = R.drawable.cat2, text = "Katze 2", size = 100),
    Item(id = R.drawable.cat3, text = "Katze 3", size = 100)
)
