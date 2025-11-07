package com.mad.myfitnesstrackingapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.screens.ui.RecentWorkoutItem
import com.mad.myfitnesstrackingapp.db.WorkoutPreview
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import java.time.LocalDateTime



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {

    // Create a longer list of demo data for this screen
    // This list now correctly creates 'db.WorkoutPreview' objects
    val demoHistory = listOf(
        WorkoutPreview(1, "Running", 40, 6.2, LocalDateTime.now().minusDays(0)),
        WorkoutPreview(2, "Cycling", 60, 20.4, LocalDateTime.now().minusDays(1)),
        WorkoutPreview(3, "Weightlifting", 50, null, LocalDateTime.now().minusDays(2)),
        WorkoutPreview(4, "Running", 35, 5.1, LocalDateTime.now().minusDays(4)),
        WorkoutPreview(5, "Weightlifting", 45, null, LocalDateTime.now().minusDays(5)),
        WorkoutPreview(6, "Cycling", 75, 25.0, LocalDateTime.now().minusDays(6)),
        WorkoutPreview(7, "Running", 42, 6.5, LocalDateTime.now().minusDays(7)),
        WorkoutPreview(8, "Weightlifting", 55, null, LocalDateTime.now().minusDays(9)),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientTop
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(listOf(GradientTop, GradientMid, GradientBottom))
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(demoHistory) { workout ->
                    RecentWorkoutItem(workout = workout, onClick = { /* TODO */ })
                }
            }
        }
    }
}
