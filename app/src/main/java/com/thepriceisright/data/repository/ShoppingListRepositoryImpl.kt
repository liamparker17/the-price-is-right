package com.thepriceisright.data.repository

import com.thepriceisright.data.local.ShoppingListDataStore
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.model.ShoppingList
import com.thepriceisright.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val dataStore: ShoppingListDataStore
) : ShoppingListRepository {

    override fun getAllLists(): Flow<List<ShoppingList>> = dataStore.getAllLists()

    override suspend fun getListById(id: String): Resource<ShoppingList> {
        val lists = dataStore.getAllLists().first()
        val list = lists.find { it.id == id }
        return if (list != null) {
            Resource.Success(list)
        } else {
            Resource.Error("Shopping list not found")
        }
    }

    override suspend fun createList(list: ShoppingList): Resource<ShoppingList> {
        return try {
            val currentLists = dataStore.getAllLists().first().toMutableList()
            currentLists.add(list)
            dataStore.saveLists(currentLists)
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error("Failed to create shopping list: ${e.message}")
        }
    }

    override suspend fun updateList(list: ShoppingList): Resource<Unit> {
        return try {
            val currentLists = dataStore.getAllLists().first().toMutableList()
            val index = currentLists.indexOfFirst { it.id == list.id }
            if (index == -1) {
                return Resource.Error("Shopping list not found")
            }
            currentLists[index] = list.copy(updatedAt = LocalDateTime.now())
            dataStore.saveLists(currentLists)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update shopping list: ${e.message}")
        }
    }

    override suspend fun deleteList(id: String): Resource<Unit> {
        return try {
            val currentLists = dataStore.getAllLists().first().toMutableList()
            currentLists.removeAll { it.id == id }
            dataStore.saveLists(currentLists)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete shopping list: ${e.message}")
        }
    }

    override suspend fun generateShareCode(listId: String): Resource<String> {
        return try {
            val currentLists = dataStore.getAllLists().first().toMutableList()
            val index = currentLists.indexOfFirst { it.id == listId }
            if (index == -1) {
                return Resource.Error("Shopping list not found")
            }

            val shareCode = generateAlphanumericCode(6)
            currentLists[index] = currentLists[index].copy(
                shareCode = shareCode,
                isShared = true,
                updatedAt = LocalDateTime.now()
            )
            dataStore.saveLists(currentLists)
            Resource.Success(shareCode)
        } catch (e: Exception) {
            Resource.Error("Failed to generate share code: ${e.message}")
        }
    }

    override suspend fun importFromShareCode(code: String): Resource<ShoppingList> {
        // In a real app, this would call a backend API to fetch the shared list.
        // For now, we search local lists for the share code (demo/offline mode).
        val lists = dataStore.getAllLists().first()
        val sharedList = lists.find { it.shareCode == code }
        return if (sharedList != null) {
            // Create a copy with a new ID for the importing user
            val importedList = sharedList.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = "${sharedList.name} (imported)",
                shareCode = null,
                isShared = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            createList(importedList)
        } else {
            Resource.Error("No shopping list found with share code: $code")
        }
    }

    /**
     * Generates a random 6-character alphanumeric share code.
     */
    private fun generateAlphanumericCode(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
}
