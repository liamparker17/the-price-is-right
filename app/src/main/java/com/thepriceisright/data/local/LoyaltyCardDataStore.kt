package com.thepriceisright.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thepriceisright.domain.model.BarcodeFormat
import com.thepriceisright.domain.model.LoyaltyCard
import com.thepriceisright.domain.model.LoyaltyProgram
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.loyaltyCardDataStore: DataStore<Preferences> by preferencesDataStore(name = "loyalty_cards")

@Singleton
class LoyaltyCardDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    private val cardsKey = stringPreferencesKey("cards")
    private val listType = Types.newParameterizedType(List::class.java, LoyaltyCardData::class.java)

    fun getAllCards(): Flow<List<LoyaltyCard>> = context.loyaltyCardDataStore.data.map { prefs ->
        val json = prefs[cardsKey] ?: "[]"
        deserializeCards(json)
    }

    suspend fun saveCards(cards: List<LoyaltyCard>) {
        context.loyaltyCardDataStore.edit { prefs ->
            prefs[cardsKey] = serializeCards(cards)
        }
    }

    private fun serializeCards(cards: List<LoyaltyCard>): String {
        try {
            val dataList = cards.map { it.toData() }
            val adapter = moshi.adapter<List<LoyaltyCardData>>(listType)
            return adapter.toJson(dataList)
        } catch (e: Exception) {
            return "[]"
        }
    }

    private fun deserializeCards(json: String): List<LoyaltyCard> {
        try {
            val adapter = moshi.adapter<List<LoyaltyCardData>>(listType)
            return adapter.fromJson(json)?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }
}

data class LoyaltyCardData(
    val id: String,
    val cardName: String,
    val programName: String,
    val barcodeValue: String,
    val barcodeFormat: String,
    val cardholderName: String?,
    val cardNumber: String?,
    val colorHex: String
) {
    fun toDomain(): LoyaltyCard = LoyaltyCard(
        id = id,
        cardName = cardName,
        programName = LoyaltyProgram.valueOf(programName),
        barcodeValue = barcodeValue,
        barcodeFormat = BarcodeFormat.valueOf(barcodeFormat),
        cardholderName = cardholderName,
        cardNumber = cardNumber,
        colorHex = colorHex
    )
}

fun LoyaltyCard.toData(): LoyaltyCardData = LoyaltyCardData(
    id = id,
    cardName = cardName,
    programName = programName.name,
    barcodeValue = barcodeValue,
    barcodeFormat = barcodeFormat.name,
    cardholderName = cardholderName,
    cardNumber = cardNumber,
    colorHex = colorHex
)
