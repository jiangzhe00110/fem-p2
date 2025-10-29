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
import com.example.fem_p2.data.weather.WeatherRepository
import com.example.fem_p2.data.weather.model.WeatherSummary
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val itineraryRepository: ItineraryRepository,
    private val weatherRepository: WeatherRepository
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
                            weatherSummary = null
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
                    refreshWeather()
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

    fun refreshWeather() {
        _uiState.update { it.copy(isLoadingWeather = true, weatherError = null) }
        viewModelScope.launch {
            val result = weatherRepository.fetchMadridSummary()
            result.onSuccess { summary ->
                _uiState.update {
                    it.copy(weatherSummary = summary, isLoadingWeather = false)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        weatherSummary = null,
                        weatherError = error.localizedMessage ?: "No se pudo obtener el clima",
                        isLoadingWeather = false
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
                        newEntryDescription = ""
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
                    weatherRepository = app.appContainer.weatherRepository
                )
            }
        }
    }
}

data class HomeUiState(
    val isSignedIn: Boolean = false,
    val userName: String = "",
    val weatherSummary: WeatherSummary? = null,
    val weatherError: String? = null,
    val isLoadingWeather: Boolean = false,
    val itineraries: List<TravelEntry> = emptyList(),
    val isDialogVisible: Boolean = false,
    val newEntryTitle: String = "",
    val newEntryDescription: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)