package com.thepriceisright.domain.repository

import com.thepriceisright.domain.model.LoyaltyCard
import com.thepriceisright.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface LoyaltyCardRepository {
    fun getAllCards(): Flow<List<LoyaltyCard>>
    suspend fun addCard(card: LoyaltyCard): Resource<Unit>
    suspend fun deleteCard(id: String): Resource<Unit>
    suspend fun updateCard(card: LoyaltyCard): Resource<Unit>
}
