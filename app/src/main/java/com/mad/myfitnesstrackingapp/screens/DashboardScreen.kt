package com.mad.myfitnesstrackingapp.screens

import android.os.Build

import androidx.annotation.RequiresApi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.navigation.NavRoute
import com.mad.myfitnesstrackingapp.ui.theme.*
import com.mad.myfitnesstrackingapp.R
// --- FIX: Import the correct ViewModel from the 'db' package ---
import com.mad.myfitnesstrackingapp.db.Workout_Db_connection
import com.mad.myfitnesstrackingapp.screens.ui.Goal
import com.mad.myfitnesstrackingapp.screens.ui.GoalProgressCard
import com.mad.myfitnesstrackingapp.screens.ui.QuickAction
import com.mad.myfitnesstrackingapp.screens.ui.QuickActionsGrid
import com.mad.myfitnesstrackingapp.screens.ui.RecentWorkoutItem
import com.mad.myfitnesstrackingapp.screens.ui.SummaryCard



// --- Dashboard Screen Composable ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    // 1. Inject the correct WorkoutViewModel (from 'db' package)
    workoutViewModel: Workout_Db_connection = viewModel(),
) {
    // 2. Collect state from the ViewModel
    val userName by workoutViewModel.username.collectAsState()
    val summary by workoutViewModel.summary.collectAsState()
    val recentWorkouts by workoutViewModel.recentWorkouts.collectAsState()

    // 3. ---REMOVED---
    // The correct ViewModel loads data in its 'init' block,
    // so we don't need to call it again from here.

    LaunchedEffect(Unit) {
        workoutViewModel.loadData()
    }


    // Mock data for goals (since it's not in the backend yet)
    val populatedGoals = listOf(
        Goal(1, "Run 20 km this week", 60),
        Goal(2, "Workout 4x this week", 75),
        Goal(3, "Cycle 100km this month", 30),
    )

    // 4. Define the logout function
    val onLogoutClick: () -> Unit = {
        workoutViewModel.logout()
        // Navigate back to login and clear the back stack
        navController.navigate(NavRoute.LOGIN) {
            popUpTo(NavRoute.HOME) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(GradientTop, GradientMid, GradientBottom))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { navController.navigate(NavRoute.PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        // Use the username from the ViewModel
                        userName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Greeting
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hello,", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                    // This will now show the correct name
                    Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigate(NavRoute.SETTINGS) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Summary Cards ---
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    SummaryCard(
                        title = "Workouts",
                        value = summary.totalWorkouts.toString(),
                        subtitle = "Total",
                        modifier = Modifier.width(120.dp)
                    )
                }
                item {
                    SummaryCard(
                        title = "Time",
                        value = "${summary.totalMinutes / 60}h ${summary.totalMinutes % 60}m",
                        subtitle = "Total",
                        modifier = Modifier.width(120.dp)
                    )
                }
                item {
                    SummaryCard(
                        title = "Distance",
                        value = "${summary.totalDistanceKm} km",
                        subtitle = "Total",
                        modifier = Modifier.width(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Goals ---
            Text("Goals", color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(populatedGoals) { g ->
                    GoalProgressCard(goal = g, modifier = Modifier.width(170.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Quick Actions ---
            val quickActions = listOf(
                QuickAction(
                    "Workouts",
                    drawableRes = R.drawable.ic_score_24px, // replace with your workout icon if available
                    onClick = { navController.navigate(NavRoute.WORKOUTS_LIST) }
                ),
                QuickAction(
                    "Goals",
                    drawableRes = R.drawable.goals_24px,
                    onClick = { navController.navigate(NavRoute.GOALS) }
                ),
            )
            QuickActionsGrid(actions = quickActions)


            Spacer(modifier = Modifier.height(16.dp))

            // --- Recent Workouts Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Workouts",
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { navController.navigate(NavRoute.HISTORY) }) {
                    Text("See all", color = Color.White.copy(alpha = 0.9f))
                }
            }

            // --- Scrollable Recent Workouts List ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // <-- makes only this scrollable
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentWorkouts) { workout ->
                    RecentWorkoutItem(workout = workout,
                        onClick = { navController.navigate(NavRoute.HISTORY) })
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { navController.navigate(NavRoute.WORKOUT) },
            containerColor = PrimaryBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Workout", tint = Color.White)
        }
    }

}
