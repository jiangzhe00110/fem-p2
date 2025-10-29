package com.example.fem_p2.data.weather

import com.example.fem_p2.data.weather.model.WeatherSummary

class WeatherRepository(private val service: WeatherService) {
    suspend fun fetchMadridSummary(): Result<WeatherSummary> = runCatching {
        val response = service.getMadridWeather()
        val currentWeather = response.currentWeather
            ?: throw IllegalStateException("La API no devolvió el clima actual")
        WeatherSummary(
            temperature = currentWeather.temperature,
            windSpeed = currentWeather.windspeed,
            weatherCode = currentWeather.weathercode,
            observationTime = currentWeather.time
        )
    }
}