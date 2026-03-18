package com.thepriceisright.domain.repository

import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getAllLists(): Flow<List<ShoppingList>>
    suspend fun getListById(id: String): Resource<ShoppingList>
    suspend fun createList(list: ShoppingList): Resource<ShoppingList>
    suspend fun updateList(list: ShoppingList): Resource<Unit>
    suspend fun deleteList(id: String): Resource<Unit>
    suspend fun generateShareCode(listId: String): Resource<String>
    suspend fun importFromShareCode(code: String): Resource<ShoppingList>
}
