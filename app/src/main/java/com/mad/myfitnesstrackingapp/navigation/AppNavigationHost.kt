package com.mad.myfitnesstrackingapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.mad.myfitnesstrackingapp.db.Workout_Db_connection

import com.mad.myfitnesstrackingapp.screens.AddWorkoutScreen
import com.mad.myfitnesstrackingapp.screens.DashboardScreen
import com.mad.myfitnesstrackingapp.screens.GoalsScreen
import com.mad.myfitnesstrackingapp.screens.HistoryScreen
import com.mad.myfitnesstrackingapp.screens.LoginScreen
import com.mad.myfitnesstrackingapp.screens.ProfileScreen
import com.mad.myfitnesstrackingapp.screens.RegisterScreen
import com.mad.myfitnesstrackingapp.screens.SettingsScreen
import com.mad.myfitnesstrackingapp.screens.WorkoutsListScreen
import com.mad.myfitnesstrackingapp.screens.workouts.RunningScreen
import com.mad.myfitnesstrackingapp.screens.workouts.WalkingScreen
import com.mad.myfitnesstrackingapp.screens.workouts.CyclingScreen
import com.mad.myfitnesstrackingapp.screens.workouts.WeightliftingScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoute.WORKOUTS_LIST,
        modifier = modifier
    ) {

        // Login
        composable(NavRoute.LOGIN) { LoginScreen(navController) }

        // Register
        composable(NavRoute.REGISTER) { RegisterScreen(navController) }

        // Dashboard
        composable(NavRoute.HOME) { DashboardScreen(navController) }

        // Manual Add Workout
        composable(NavRoute.WORKOUT) {
            val workoutViewModel: Workout_Db_connection = viewModel()
            val onSaveAction = { navController.popBackStack() }
            AddWorkoutScreen(
                addWorkout = workoutViewModel,
                onWorkoutSaved = onSaveAction
            )
        }

        // Workouts list screen
        composable(NavRoute.WORKOUTS_LIST) {
            WorkoutsListScreen(navController)
        }

        // Explicit activity routes
        composable(NavRoute.RUNNING) { RunningScreen(navController) }
        composable(NavRoute.WALKING) { WalkingScreen(navController) }
        composable(NavRoute.CYCLING) { CyclingScreen(navController) }
        composable(NavRoute.WEIGHTLIFTING) { WeightliftingScreen(navController) }

        // Settings
        composable(NavRoute.SETTINGS) { SettingsScreen(navController) }

        // History
        composable(NavRoute.HISTORY) { HistoryScreen(navController) }

        // Profile (passes VM)
        composable(NavRoute.PROFILE) {
            val workoutViewModel: Workout_Db_connection = viewModel()
            ProfileScreen(navController, workoutViewModel)
        }

        // Goals
        composable(NavRoute.GOALS) { GoalsScreen(navController) }
    }
}

object NavRoute {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val WORKOUT = "workout"           // manual add
    const val WORKOUTS_LIST = "workouts_list" // new list screen

    // explicit routes for each activity
    const val RUNNING = "running"
    const val WALKING = "walking"
    const val CYCLING = "cycling"
    const val WEIGHTLIFTING = "weightlifting"

    const val SETTINGS = "settings"
    const val HISTORY = "history"
    const val GOALS = "goals"
    const val PROFILE = "profile"
}
