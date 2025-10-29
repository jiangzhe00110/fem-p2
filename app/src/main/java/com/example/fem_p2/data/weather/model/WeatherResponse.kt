package com.example.fem_p2.data.weather.model

import com.squareup.moshi.Json

data class WeatherResponse(
    @Json(name = "current_weather")
    val currentWeather: CurrentWeather?
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    @Json(name = "weathercode")
    val weathercode: Int,
    val time: String
)

data class WeatherSummary(
    val temperature: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val observationTime: String
)