package com.thepriceisright.data.repository

import com.thepriceisright.data.local.UserPreferencesDataStore
import com.thepriceisright.domain.model.UserPreferences
import com.thepriceisright.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override fun getPreferences(): Flow<UserPreferences> = dataStore.getPreferences()

    override suspend fun updatePreferences(preferences: UserPreferences) {
        dataStore.savePreferences(preferences)
    }
}
