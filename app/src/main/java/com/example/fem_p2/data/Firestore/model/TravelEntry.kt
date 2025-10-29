package com.example.fem_p2.data.firestore.model

import com.google.firebase.Timestamp

data class TravelEntry(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now()
)