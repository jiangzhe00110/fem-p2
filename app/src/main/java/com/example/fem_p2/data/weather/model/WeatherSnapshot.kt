package com.example.fem_p2.data.weather.model

import java.time.Instant

/**
 * Represents a weather reading captured from the remote API along with the moment it was fetched
 * by the application. The [WeatherSummary] contains the observation reported by the API, while the
 * [fetchedAt] timestamp tracks when the app retrieved that information so it can be surfaced in the
 * weather history dialog.
 */
data class WeatherSnapshot(
    val summary: WeatherSummary,
    val fetchedAt: Instant,
)