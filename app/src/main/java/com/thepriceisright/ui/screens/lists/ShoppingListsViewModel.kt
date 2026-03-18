package com.thepriceisright.ui.screens.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListsUiState(
    val lists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = true,
    val showCreateDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val importCode: String = "",
    val importResult: Resource<ShoppingList>? = null
)

@HiltViewModel
class ShoppingListsViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            shoppingListRepository.getAllLists().collect { lists ->
                _uiState.update { it.copy(lists = lists, isLoading = false) }
            }
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            shoppingListRepository.createList(ShoppingList(name = name))
            _uiState.update { it.copy(showCreateDialog = false) }
        }
    }

    fun deleteList(id: String) {
        viewModelScope.launch { shoppingListRepository.deleteList(id) }
    }

    fun showCreateDialog() { _uiState.update { it.copy(showCreateDialog = true) } }
    fun hideCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun showImportDialog() { _uiState.update { it.copy(showImportDialog = true) } }
    fun hideImportDialog() { _uiState.update { it.copy(showImportDialog = false) } }

    fun onImportCodeChanged(code: String) { _uiState.update { it.copy(importCode = code) } }

    fun importList() {
        val code = _uiState.value.importCode
        if (code.isBlank()) return
        viewModelScope.launch {
            val result = shoppingListRepository.importFromShareCode(code)
            _uiState.update { it.copy(importResult = result, showImportDialog = false) }
        }
    }
}
