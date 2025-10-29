package com.example.fem_p2.data.weather

import com.example.fem_p2.data.weather.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("v1/forecast")
    suspend fun getMadridWeather(
        @Query("latitude") latitude: Double = 40.4168,
        @Query("longitude") longitude: Double = -3.7038,
        @Query("current_weather") includeCurrentWeather: Boolean = true,
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}