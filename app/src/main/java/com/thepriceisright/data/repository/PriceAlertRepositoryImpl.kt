package com.thepriceisright.data.repository

import com.thepriceisright.data.local.PriceAlertDataStore
import com.thepriceisright.domain.model.PriceAlert
import com.thepriceisright.domain.model.Resource
import com.thepriceisright.domain.repository.PriceAlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceAlertRepositoryImpl @Inject constructor(
    private val dataStore: PriceAlertDataStore
) : PriceAlertRepository {

    override fun getActiveAlerts(): Flow<List<PriceAlert>> =
        dataStore.getAllAlerts().map { alerts ->
            alerts.filter { it.isActive }
        }

    override suspend fun addAlert(alert: PriceAlert): Resource<Unit> {
        return try {
            val currentAlerts = dataStore.getAllAlerts().first().toMutableList()
            currentAlerts.add(alert)
            dataStore.saveAlerts(currentAlerts)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to add price alert: ${e.message}")
        }
    }

    override suspend fun removeAlert(id: String): Resource<Unit> {
        return try {
            val currentAlerts = dataStore.getAllAlerts().first().toMutableList()
            currentAlerts.removeAll { it.id == id }
            dataStore.saveAlerts(currentAlerts)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to remove price alert: ${e.message}")
        }
    }

    override suspend fun updateAlert(alert: PriceAlert): Resource<Unit> {
        return try {
            val currentAlerts = dataStore.getAllAlerts().first().toMutableList()
            val index = currentAlerts.indexOfFirst { it.id == alert.id }
            if (index == -1) {
                return Resource.Error("Price alert not found")
            }
            currentAlerts[index] = alert
            dataStore.saveAlerts(currentAlerts)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update price alert: ${e.message}")
        }
    }
}
