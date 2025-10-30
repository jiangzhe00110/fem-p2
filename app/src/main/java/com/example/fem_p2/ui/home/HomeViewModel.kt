package com.example.fem_p2.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fem_p2.TravelPlannerApp
import com.example.fem_p2.data.auth.AuthRepository
import com.example.fem_p2.data.firestore.ItineraryRepository
import com.example.fem_p2.data.firestore.model.TravelEntry
import com.example.fem_p2.data.news.NewsRepository
import com.example.fem_p2.data.weather.WeatherRepository
import com.example.fem_p2.data.weather.model.WeatherSnapshot
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

private const val MAX_HISTORY_ENTRIES = 10

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val itineraryRepository: ItineraryRepository,
    private val weatherRepository: WeatherRepository,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var itinerariesJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                if (user == null) {
                    itinerariesJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isSignedIn = false,
                            itineraries = emptyList(),
                            userName = "",
                            currentWeather = null,
                            weatherError = null,
                            isLoadingWeather = false,
                            weatherHistory = emptyList(),
                            isHistoryDialogVisible = false,
                            newsHeadlines = emptyList(),
                            newsError = null,
                            isLoadingNews = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSignedIn = true,
                            userName = user.email ?: user.displayName ?: user.uid
                        )
                    }
                    observeItineraries(user.uid)
                    refreshDashboard()
                }
            }
        }
    }

    private fun observeItineraries(userId: String) {
        itinerariesJob?.cancel()
        itinerariesJob = viewModelScope.launch {
            itineraryRepository.observeItineraries(userId)
                .catch { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.localizedMessage ?: "No se pudieron cargar tus planes")
                    }
                }
                .collect { entries ->
                    _uiState.update { it.copy(itineraries = entries) }
                }
        }
    }

    fun refreshDashboard() {
        refreshWeather()
        refreshNews()
    }

    private fun refreshWeather() {
        _uiState.update { it.copy(isLoadingWeather = true, weatherError = null) }
        viewModelScope.launch {
            val result = weatherRepository.fetchMadridSummary()
            result.onSuccess { summary ->
                val snapshot = WeatherSnapshot(summary = summary, fetchedAt = Instant.now())
                _uiState.update { current ->
                    val updatedHistory = current.currentWeather?.let { previous ->
                        listOf(previous) + current.weatherHistory
                    } ?: current.weatherHistory

                    current.copy(
                        currentWeather = snapshot,
                        isLoadingWeather = false,
                        weatherHistory = updatedHistory.take(MAX_HISTORY_ENTRIES)
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        weatherError = error.localizedMessage ?: "No se pudo obtener el clima",
                        isLoadingWeather = false
                    )
                }
            }
        }
    }

    private fun refreshNews() {
        _uiState.update { it.copy(isLoadingNews = true, newsError = null) }
        viewModelScope.launch {
            val result = newsRepository.fetchMadridHeadlines()
            result.onSuccess { headlines ->
                _uiState.update {
                    it.copy(
                        newsHeadlines = headlines,
                        isLoadingNews = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        newsHeadlines = emptyList(),
                        newsError = error.localizedMessage ?: "No se pudieron obtener las noticias",
                        isLoadingNews = false
                    )
                }
            }
        }
    }

    fun updateNewTitle(value: String) {
        _uiState.update { it.copy(newEntryTitle = value) }
    }

    fun updateNewDescription(value: String) {
        _uiState.update { it.copy(newEntryDescription = value) }
    }

    fun toggleDialog(show: Boolean) {
        _uiState.update { it.copy(isDialogVisible = show, errorMessage = null) }
    }

    fun toggleWeatherHistory(show: Boolean) {
        _uiState.update { it.copy(isHistoryDialogVisible = show) }
    }

    fun saveEntry() {
        val user = authRepository.currentUser() ?: return
        val state = _uiState.value
        if (state.newEntryTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Añade un título para tu plan") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val entry = TravelEntry(
                title = state.newEntryTitle.trim(),
                description = state.newEntryDescription.trim(),
                timestamp = Timestamp.now()
            )
            val result = itineraryRepository.addEntry(user.uid, entry)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isDialogVisible = false,
                        newEntryTitle = "",
                        newEntryDescription = "",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.localizedMessage ?: "No se pudo guardar el plan"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun signOut() {
        authRepository.signOut()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TravelPlannerApp)
                HomeViewModel(
                    authRepository = app.appContainer.authRepository,
                    itineraryRepository = app.appContainer.itineraryRepository,
                    weatherRepository = app.appContainer.weatherRepository,
                    newsRepository = app.appContainer.newsRepository
                )
            }
        }
    }
}

data class HomeUiState(
    val isSignedIn: Boolean = false,
    val userName: String = "",
    val currentWeather: WeatherSnapshot? = null,
    val weatherError: String? = null,
    val isLoadingWeather: Boolean = false,
    val weatherHistory: List<WeatherSnapshot> = emptyList(),
    val isHistoryDialogVisible: Boolean = false,
    val newsHeadlines: List<String> = emptyList(),
    val newsError: String? = null,
    val isLoadingNews: Boolean = false,
    val itineraries: List<TravelEntry> = emptyList(),
    val isDialogVisible: Boolean = false,
    val newEntryTitle: String = "",
    val newEntryDescription: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
