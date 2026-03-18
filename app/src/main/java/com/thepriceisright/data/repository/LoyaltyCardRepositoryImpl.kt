package com.thepriceisright.data.repository

import com.thepriceisright.data.local.LoyaltyCardDataStore
import com.thepriceisright.domain.model.LoyaltyCard
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.repository.LoyaltyCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyCardRepositoryImpl @Inject constructor(
    private val dataStore: LoyaltyCardDataStore
) : LoyaltyCardRepository {

    override fun getAllCards(): Flow<List<LoyaltyCard>> = dataStore.getAllCards()

    override suspend fun addCard(card: LoyaltyCard): Resource<Unit> {
        return try {
            val currentCards = dataStore.getAllCards().first().toMutableList()
            currentCards.add(card)
            dataStore.saveCards(currentCards)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to add loyalty card: ${e.message}")
        }
    }

    override suspend fun deleteCard(id: String): Resource<Unit> {
        return try {
            val currentCards = dataStore.getAllCards().first().toMutableList()
            currentCards.removeAll { it.id == id }
            dataStore.saveCards(currentCards)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete loyalty card: ${e.message}")
        }
    }

    override suspend fun updateCard(card: LoyaltyCard): Resource<Unit> {
        return try {
            val currentCards = dataStore.getAllCards().first().toMutableList()
            val index = currentCards.indexOfFirst { it.id == card.id }
            if (index == -1) {
                return Resource.Error("Loyalty card not found")
            }
            currentCards[index] = card
            dataStore.saveCards(currentCards)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update loyalty card: ${e.message}")
        }
    }
}
