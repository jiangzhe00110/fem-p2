package com.example.fem_p2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fem_p2.ui.auth.AuthScreen
import com.example.fem_p2.ui.auth.AuthViewModel
import com.example.fem_p2.ui.home.HomeScreen
import com.example.fem_p2.ui.home.HomeViewModel

private object Destinations {
    const val AUTH = "auth"
    const val HOME = "home"
}

@Composable
fun TravelPlannerNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Destinations.AUTH) {
        composable(Destinations.AUTH) {
            AuthScreen(
                state = authState,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onConfirmPasswordChange = authViewModel::updateConfirmPassword,
                onSubmit = authViewModel::submit,
                onToggleMode = authViewModel::toggleMode,
                onAuthenticated = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.HOME) {
            HomeScreen(
                state = homeState,
                onRefreshWeather = homeViewModel::refreshWeather,
                onSignOut = {
                    homeViewModel.signOut()
                    navController.navigate(Destinations.AUTH) {
                        popUpTo(Destinations.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onShowDialog = homeViewModel::toggleDialog,
                onTitleChange = homeViewModel::updateNewTitle,
                onDescriptionChange = homeViewModel::updateNewDescription,
                onSaveEntry = homeViewModel::saveEntry,
                onErrorConsumed = homeViewModel::clearError
            )
        }
    }
}