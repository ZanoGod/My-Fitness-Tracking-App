package com.mad.myfitnesstrackingapp.screens.workouts

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun WalkingScreen(navController: NavController) {
    ActivityTrackScreen(navController = navController, type = ActivityType.WALKING)
}
