package com.thepriceisright.domain.repository

import com.thepriceisright.domain.model.PriceAlert
import com.thepriceisright.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface PriceAlertRepository {
    fun getActiveAlerts(): Flow<List<PriceAlert>>
    suspend fun addAlert(alert: PriceAlert): Resource<Unit>
    suspend fun removeAlert(id: String): Resource<Unit>
    suspend fun updateAlert(alert: PriceAlert): Resource<Unit>
}
