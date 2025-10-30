package com.example.fem_p2

import com.example.fem_p2.data.auth.AuthRepository
import com.example.fem_p2.data.auth.FirebaseAuthRepository
import com.example.fem_p2.data.firestore.ItineraryRepository
import com.example.fem_p2.data.weather.WeatherRepository
import com.example.fem_p2.data.weather.WeatherService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.example.fem_p2.data.firestore.FirestoreItineraryRepository
//import com.example.fem_p2.data.firestore.ItineraryRepository
import com.example.fem_p2.data.news.NewsRepository

class TravelPlannerContainer {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val weatherService: WeatherService = retrofit.create(WeatherService::class.java)

    val weatherRepository: WeatherRepository = WeatherRepository(weatherService)

    val newsRepository: NewsRepository = NewsRepository(okHttpClient)
    private val firebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore

    val authRepository: AuthRepository = FirebaseAuthRepository(firebaseAuth)
    val itineraryRepository: ItineraryRepository = FirestoreItineraryRepository(firestore)
}