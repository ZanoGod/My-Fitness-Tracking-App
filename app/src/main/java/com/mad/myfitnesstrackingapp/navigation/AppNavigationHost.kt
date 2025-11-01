package com.mad.myfitnesstrackingapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mad.myfitnesstrackingapp.screens.DashboardScreen
import com.mad.myfitnesstrackingapp.screens.LoginScreen
import com.mad.myfitnesstrackingapp.screens.RegisterScreen

@Composable
fun Navigation() { // Renamed from AppNavigation
    // Create the navigation controller
    val navController = rememberNavController()

    // Auth logic has been removed to allow free navigation

    // This is the NavHost. It switches between your screens.
    NavHost(
        navController = navController,
        startDestination = NavRoute.LOGIN // Set start destination to Login
    ) {

        // Login Screen Route
        composable(NavRoute.LOGIN) {
            LoginScreen(
                onLoginClick = { email, password ->
                    // Freely navigate to dashboard
                    navController.navigate(NavRoute.HOME) {
                        // Clear login from back stack
                        popUpTo(NavRoute.LOGIN) { inclusive = true }
                    }
                },
                onRegisterNavigate = {
                    navController.navigate(NavRoute.REGISTER)
                }
            )
        }

        // Register Screen Route
        composable(NavRoute.REGISTER) {
            RegisterScreen(
                onRegisterClick = { username, email, password ->
                    // After "registering", go back to login
                    navController.navigate(NavRoute.LOGIN) {
                        // Clear register from back stack
                        popUpTo(NavRoute.REGISTER) { inclusive = true }
                    }
                },
                onLoginNavigate = {
                    navController.popBackStack() // Go back to login
                }
            )
        }

        // Dashboard Screen Route
        composable(NavRoute.HOME) {
            DashboardScreen(
                onLogoutClick = {
                    // Log out and return to login
                    navController.navigate(NavRoute.LOGIN) {
                        // Clear dashboard from back stack
                        popUpTo(NavRoute.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Defines all the navigation routes in the app
object NavRoute {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
}

