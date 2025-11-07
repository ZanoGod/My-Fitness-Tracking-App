package com.mad.myfitnesstrackingapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.mad.myfitnesstrackingapp.screens.ui.Goal
import com.mad.myfitnesstrackingapp.screens.ui.GoalProgressCard
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.ui.theme.PrimaryBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController) {

    // Create demo data for this screen
    val demoGoals = listOf(
        Goal(1, "Run 20 km this week", 60),
        Goal(2, "Workout 4x this week", 75),
        Goal(3, "Cycle 100km this month", 30),
        Goal(4, "Lift a new PR", 0)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Goals", fontWeight = FontWeight.Bold, color = Color.White) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Show Add Goal Dialog */ },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.White)
            }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(demoGoals) { goal ->
                    // Use the same GoalProgressCard from the dashboard UI file
                    GoalProgressCard(
                        goal = goal,
                        modifier = Modifier.fillMaxWidth() // Make it full width
                    )
                }
            }
        }
    }
}

