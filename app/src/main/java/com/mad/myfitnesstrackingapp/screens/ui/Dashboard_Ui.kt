package com.mad.myfitnesstrackingapp.screens.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- FIX 1: Import the correct WorkoutPreview class ---
import com.mad.myfitnesstrackingapp.db.WorkoutPreview
import com.mad.myfitnesstrackingapp.ui.theme.SecondaryCyan
import java.time.format.DateTimeFormatter

// --- Small building block composables ---
// These were moved from DashboardScreen.kt


@Composable
internal fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}


@Composable
internal fun GoalProgressCard(goal: Goal, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular progress imitation
            val progress = (goal.progressPercent / 100f)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                // simple circular indicator using CircularProgressIndicator
                CircularProgressIndicator(
                    progress = { progress }, // M3 API requires a lambda
                    strokeWidth = 6.dp,
                    modifier = Modifier.fillMaxSize(),
                    color = SecondaryCyan,
                    trackColor = Color.White.copy(alpha = 0.2f) // Added track color
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${goal.progressPercent}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Complete", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                goal.title,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


// --- Card-style Quick Actions Grid ---
@Composable
internal fun QuickActionsGrid(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier
) {
    // Two columns grid
    val chunkedActions = actions.chunked(2) // 2 cards per row
    Column(modifier = modifier) {
        chunkedActions.forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowActions.forEach { action ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clickable { action.onClick() }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            action.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = action.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            action.drawableRes?.let {
                                Icon(
                                    painter = painterResource(id = it),
                                    contentDescription = action.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = action.label,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // Fill empty space if row has only one item
                if (rowActions.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
// --- FIX 1: Change the type to match the ViewModel ---
// The ViewModel provides 'db.WorkoutPreview', so this composable should expect that type.
internal fun RecentWorkoutItem(workout: WorkoutPreview, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd • HH:mm")
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small icon box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                // You can use vector icons or local drawables for each activity type
                val emoji = when {
                    workout.type.contains("running", true) -> "🏃"
                    workout.type.contains("cycling", true) -> "🚴"
                    workout.type.contains("weightlifting", true) -> "🏋️"
                    workout.type.contains("walking",true) -> "🚶"
                    else -> "⚡"
                }
                Text(emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(workout.type, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${workout.durationMin} min${workout.distanceKm?.let { " • $it km" } ?: ""}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }

            Text(
                workout.date.format(formatter),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}
