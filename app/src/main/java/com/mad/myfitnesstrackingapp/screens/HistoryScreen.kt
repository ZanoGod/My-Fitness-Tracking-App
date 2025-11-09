package com.mad.myfitnesstrackingapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.myfitnesstrackingapp.db.WorkoutDbViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    workoutViewModel: WorkoutDbViewModel = viewModel()
) {
    // Collect state exposed by ViewModel (see ViewModel example below)
    val history by workoutViewModel.history.collectAsState()
    val isOnline by workoutViewModel.isOnline.collectAsState()
    val isLoading by workoutViewModel.isLoading.collectAsState()

    // Local demo data (fallback for offline / empty state)
    val demoHistory = remember {
        listOf(
            WorkoutPreview(1, "Running", 40, 6.2, LocalDateTime.now().minusDays(0)),
            WorkoutPreview(2, "Cycling", 60, 20.4, LocalDateTime.now().minusDays(1)),
            WorkoutPreview(3, "Weightlifting", 50, null, LocalDateTime.now().minusDays(2)),
            WorkoutPreview(4, "Running", 35, 5.1, LocalDateTime.now().minusDays(4)),
            WorkoutPreview(5, "Weightlifting", 45, null, LocalDateTime.now().minusDays(5)),
            WorkoutPreview(6, "Cycling", 75, 25.0, LocalDateTime.now().minusDays(6)),
            WorkoutPreview(7, "Running", 42, 6.5, LocalDateTime.now().minusDays(7)),
            WorkoutPreview(8, "Weightlifting", 55, null, LocalDateTime.now().minusDays(9)),
        )
    }

    // Trigger initial load
    LaunchedEffect(Unit) {
        workoutViewModel.loadHistory()
    }

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
                actions = {
                    // Refresh button to re-fetch
                    IconButton(onClick = { workoutViewModel.loadHistory(force = true) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GradientTop)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(GradientTop, GradientMid, GradientBottom)))
        ) {
            when {
                isLoading && history.isEmpty() -> {
                    // Show a centered loader while initial load
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                (history.isEmpty() && !isOnline) -> {
                    // Offline fallback: show demo history
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                "Offline — showing local demo data",
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(demoHistory) { workout ->
                            RecentWorkoutItem(workout = workout, onClick = { /* TODO: details */ })
                        }
                    }
                }
                history.isEmpty() -> {
                    // Online but no data
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No workout history yet.", color = Color.White.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { navController.navigate("workout") }) {
                            Text("Log a workout")
                        }
                    }
                }
                else -> {
                    // Show real history returned by ViewModel
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(history) { workout ->
                            RecentWorkoutItem(workout = workout, onClick = { /* TODO: details */ })
                        }
                    }
                }
            }
        }
    }
}
