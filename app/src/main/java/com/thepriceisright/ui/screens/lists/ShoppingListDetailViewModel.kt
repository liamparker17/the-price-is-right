package com.thepriceisright.ui.screens.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListDetailUiState(
    val list: ShoppingList? = null,
    val isLoading: Boolean = true,
    val shareCode: String? = null,
    val showAddItemDialog: Boolean = false,
    val showShareDialog: Boolean = false
)

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val listId: String = savedStateHandle.get<String>("listId") ?: ""
    private val _uiState = MutableStateFlow(ShoppingListDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { loadList() }

    private fun loadList() {
        viewModelScope.launch {
            when (val result = shoppingListRepository.getListById(listId)) {
                is Resource.Success -> _uiState.update { it.copy(list = result.data, isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun addItem(name: String, quantity: Int = 1) {
        val currentList = _uiState.value.list ?: return
        val newItem = ShoppingListItem(name = name, quantity = quantity, sortOrder = currentList.items.size)
        val updated = currentList.copy(items = currentList.items + newItem)
        viewModelScope.launch {
            shoppingListRepository.updateList(updated)
            _uiState.update { it.copy(list = updated, showAddItemDialog = false) }
        }
    }

    fun toggleItem(itemId: String) {
        val currentList = _uiState.value.list ?: return
        val updated = currentList.copy(items = currentList.items.map {
            if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
        })
        viewModelScope.launch {
            shoppingListRepository.updateList(updated)
            _uiState.update { it.copy(list = updated) }
        }
    }

    fun removeItem(itemId: String) {
        val currentList = _uiState.value.list ?: return
        val updated = currentList.copy(items = currentList.items.filter { it.id != itemId })
        viewModelScope.launch {
            shoppingListRepository.updateList(updated)
            _uiState.update { it.copy(list = updated) }
        }
    }

    fun generateShareCode() {
        viewModelScope.launch {
            when (val result = shoppingListRepository.generateShareCode(listId)) {
                is Resource.Success -> _uiState.update { it.copy(shareCode = result.data, showShareDialog = true) }
                else -> {}
            }
        }
    }

    fun showAddItemDialog() { _uiState.update { it.copy(showAddItemDialog = true) } }
    fun hideAddItemDialog() { _uiState.update { it.copy(showAddItemDialog = false) } }
    fun hideShareDialog() { _uiState.update { it.copy(showShareDialog = false) } }
}
