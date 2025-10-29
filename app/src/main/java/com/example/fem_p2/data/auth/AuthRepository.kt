package com.example.fem_p2.data.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<FirebaseUser?>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    fun currentUser(): FirebaseUser?
    fun signOut()
}