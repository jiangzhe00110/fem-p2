package com.example.fem_p2.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fem_p2.R
import com.example.fem_p2.data.firestore.model.TravelEntry
import com.example.fem_p2.data.weather.model.WeatherSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefreshWeather: () -> Unit,
    onSignOut: () -> Unit,
    onShowDialog: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSaveEntry: () -> Unit,
    onErrorConsumed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onShowDialog(true) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir plan")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WeatherSection(
                        userName = state.userName,
                        summary = state.weatherSummary,
                        errorMessage = state.weatherError,
                        isLoading = state.isLoadingWeather,
                        onRefresh = onRefreshWeather
                    )
                }
                if (state.itineraries.isEmpty()) {
                    item {
                        EmptyState()
                    }
                } else {
                    items(state.itineraries) { entry ->
                        TravelEntryCard(entry = entry)
                    }
                }
            }
        }
    }

    if (state.isDialogVisible) {
        AddItineraryDialog(
            title = state.newEntryTitle,
            description = state.newEntryDescription,
            isSaving = state.isSaving,
            onDismiss = { onShowDialog(false) },
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onSave = onSaveEntry
        )
    }
}

@Composable
private fun WeatherSection(
    userName: String,
    summary: WeatherSummary?,
    errorMessage: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Hola, $userName", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            when {
                isLoading -> {
                    Text(text = "Cargando el clima actual…", style = MaterialTheme.typography.bodyMedium)
                }
                errorMessage != null -> {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRefresh) {
                        Text(text = "Reintentar")
                    }
                }
                summary != null -> {
                    Text(
                        text = "Temperatura: %.1f°C".format(summary.temperature),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Viento: %.1f km/h".format(summary.windSpeed),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Observado: ${summary.observationTime}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {
                    Text(text = "No se pudo obtener información meteorológica")
                }
            }
        }
    }
}

@Composable
private fun TravelEntryCard(entry: TravelEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.title, style = MaterialTheme.typography.titleMedium)
            if (entry.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = entry.description)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTimestamp(entry.timestamp.toDate()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aún no tienes planes guardados",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Añade experiencias y lugares que quieras visitar en Madrid.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddItineraryDialog(
    title: String,
    description: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nuevo plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSaving) {
                Text(text = if (isSaving) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

private fun formatTimestamp(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(date)
}