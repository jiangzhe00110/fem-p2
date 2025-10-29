package com.example.fem_p2.data.firestore

import com.example.fem_p2.data.firestore.model.TravelEntry
import kotlinx.coroutines.flow.Flow

interface ItineraryRepository {
    fun observeItineraries(userId: String): Flow<List<TravelEntry>>
    suspend fun addEntry(userId: String, entry: TravelEntry): Result<Unit>
}