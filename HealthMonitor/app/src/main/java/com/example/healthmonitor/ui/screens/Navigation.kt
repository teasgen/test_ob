package com.example.healthmonitor.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthmonitor.ui.screens.*
import com.example.healthmonitor.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Diary : Screen("diary")
    object Documents : Screen("documents")
    object DoctorRecords : Screen("doctor_records")
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val startDestination = if (authViewModel.isLoggedIn()) Screen.Main.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                onNavigateToDocuments = { navController.navigate(Screen.Documents.route) },
                onNavigateToDoctorRecords = { navController.navigate(Screen.DoctorRecords.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Diary.route) {
            DiaryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Documents.route) {
            DocumentsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.DoctorRecords.route) {
            DoctorRecordsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}