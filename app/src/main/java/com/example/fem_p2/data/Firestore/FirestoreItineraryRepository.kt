package com.example.fem_p2.data.firestore

import com.example.fem_p2.data.firestore.model.TravelEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

class FirestoreItineraryRepository(
    private val firestore: FirebaseFirestore
) : ItineraryRepository {

    override fun observeItineraries(userId: String): Flow<List<TravelEntry>> = callbackFlow {
        val listenerRegistration = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(ITINERARIES_COLLECTION)
            .orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents
                    ?.mapNotNull { document ->
                        document.toObject(TravelEntry::class.java)?.copy(id = document.id)
                    }
                    .orEmpty()
                trySend(entries)
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun addEntry(userId: String, entry: TravelEntry): Result<Unit> = runCatching {
        val userDocument = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
        userDocument
            .set(mapOf(OWNER_FIELD to userId), SetOptions.merge())
            .await()

        val itineraryCollection = userDocument.collection(ITINERARIES_COLLECTION)
        val newDocument = itineraryCollection.document()

        val payload = entry.copy(
            id = newDocument.id,
            ownerId = userId
        )

        newDocument
            .set(payload)
        Unit
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val ITINERARIES_COLLECTION = "itineraries"
        const val TIMESTAMP_FIELD = "timestamp"
        const val OWNER_FIELD = "ownerId"

    }
}