package com.firsty.bildtest.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.firsty.bildtest.R
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json



private const val PREFS_NAME = "my_app_prefs"
private const val KEY_ITEMS = "items_key"

data class Item(val id: Int, val uri: Uri, val text: String, val size: Int)

class ImageViewModel : ViewModel() {

    private var _items by mutableStateOf(
        emptyList<Item>()
    )


    val items: List<Item> get() = _items

    fun updateItems(newList: List<Item>,context: Context) {
        _items = newList
        saveItems(context)
    }

    fun removeImages(itemsToRemove: List<Item>, context: Context) {
        val urisToRemove = itemsToRemove.map { it.uri }.toSet()
        _items = _items.filterNot { it.uri in urisToRemove }
        saveItems(context)
    }



    fun addImagesFromUris(uris: List<Uri>) {
        val currentItems = _items
        val existingUris = currentItems.map { it.uri }.toSet()
        val existingIds = currentItems.map { it.id }.toMutableSet()

        fun generateUniqueId(): Int {
            var id = 1
            while (id in existingIds) {
                id++
            }
            existingIds.add(id)
            return id
        }

        val newItems = uris
            .filter { it !in existingUris }
            .map { uri ->
                val id = generateUniqueId()
                Item(id = id, uri = uri, text = "", size = 0)
            }

        // Update the state with a new list that includes the new items
        _items = currentItems + newItems
    }


    @Serializable
    data class ItemSurrogate(val id: Int, val uriString: String, val text: String, val size: Int)

    fun Item.toSurrogate() = ItemSurrogate(id, uri.toString(), text, size)
    fun ItemSurrogate.toItem() = Item(id, Uri.parse(uriString), text, size)



    fun saveItems(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serializableList = _items.map { it.toSurrogate() }
        val jsonString = Json.encodeToString(ListSerializer(ItemSurrogate.serializer()), serializableList)
        prefs.edit().putString(KEY_ITEMS, jsonString).apply()
    }

    fun loadItems(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_ITEMS, null) ?: return
        val serializableList = Json.decodeFromString(ListSerializer(ItemSurrogate.serializer()), jsonString)
        _items = serializableList.map { it.toItem() }
    }

    //Standard-speed slider
    var cycleInterval by mutableStateOf(10) // Standard: 10 Sekunden

    // Standard-setting for transition animation
    var transitionType by mutableStateOf(0) // 0 = Instantly, 1 = Fade, 2 = Slide
}