package com.thepriceisright.ui.screens.loyalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thepriceisright.domain.model.*
import com.thepriceisright.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoyaltyCardsUiState(
    val cards: List<LoyaltyCard> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LoyaltyCardsViewModel @Inject constructor(
    private val loyaltyCardRepository: LoyaltyCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoyaltyCardsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loyaltyCardRepository.getAllCards().collect { cards ->
                _uiState.update { it.copy(cards = cards, isLoading = false) }
            }
        }
    }

    fun addCard(card: LoyaltyCard) {
        viewModelScope.launch { loyaltyCardRepository.addCard(card) }
    }

    fun deleteCard(id: String) {
        viewModelScope.launch { loyaltyCardRepository.deleteCard(id) }
    }
}
