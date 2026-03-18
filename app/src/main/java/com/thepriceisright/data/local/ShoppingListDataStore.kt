package com.thepriceisright.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thepriceisright.domain.model.ShoppingList
import com.thepriceisright.domain.model.ShoppingListItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.shoppingListDataStore: DataStore<Preferences> by preferencesDataStore(name = "shopping_lists")

@Singleton
class ShoppingListDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    private val listsKey = stringPreferencesKey("lists")
    private val listType = Types.newParameterizedType(List::class.java, ShoppingListData::class.java)

    // Use a simple JSON adapter pattern for persistence
    // ShoppingListData is a serializable version of ShoppingList

    fun getAllLists(): Flow<List<ShoppingList>> = context.shoppingListDataStore.data.map { prefs ->
        val json = prefs[listsKey] ?: "[]"
        deserializeLists(json)
    }

    suspend fun saveLists(lists: List<ShoppingList>) {
        context.shoppingListDataStore.edit { prefs ->
            prefs[listsKey] = serializeLists(lists)
        }
    }

    private fun serializeLists(lists: List<ShoppingList>): String {
        // Simplified serialization using Moshi
        try {
            val dataList = lists.map { it.toData() }
            val adapter = moshi.adapter<List<ShoppingListData>>(listType)
            return adapter.toJson(dataList)
        } catch (e: Exception) {
            return "[]"
        }
    }

    private fun deserializeLists(json: String): List<ShoppingList> {
        try {
            val adapter = moshi.adapter<List<ShoppingListData>>(listType)
            return adapter.fromJson(json)?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }
}

// Serializable data class for DataStore
data class ShoppingListData(
    val id: String,
    val name: String,
    val items: List<ShoppingListItemData>,
    val createdAt: String,
    val updatedAt: String,
    val shareCode: String?,
    val isShared: Boolean
) {
    fun toDomain(): ShoppingList = ShoppingList(
        id = id,
        name = name,
        items = items.map { it.toDomain() },
        createdAt = java.time.LocalDateTime.parse(createdAt),
        updatedAt = java.time.LocalDateTime.parse(updatedAt),
        shareCode = shareCode,
        isShared = isShared
    )
}

data class ShoppingListItemData(
    val id: String,
    val name: String,
    val quantity: Int,
    val unit: String?,
    val isChecked: Boolean,
    val category: String?,
    val barcode: String?,
    val sortOrder: Int
) {
    fun toDomain(): ShoppingListItem = ShoppingListItem(
        id = id, name = name, quantity = quantity, unit = unit,
        isChecked = isChecked, category = category, barcode = barcode, sortOrder = sortOrder
    )
}

fun ShoppingList.toData(): ShoppingListData = ShoppingListData(
    id = id, name = name,
    items = items.map { ShoppingListItemData(it.id, it.name, it.quantity, it.unit, it.isChecked, it.category, it.barcode, it.sortOrder) },
    createdAt = createdAt.toString(), updatedAt = updatedAt.toString(),
    shareCode = shareCode, isShared = isShared
)
