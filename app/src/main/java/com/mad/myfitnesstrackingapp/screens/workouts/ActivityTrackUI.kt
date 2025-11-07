package com.mad.myfitnesstrackingapp.screens.workouts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.networks.MapWithCurrentLocation
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.ui.theme.PrimaryBlue
import com.mad.myfitnesstrackingapp.ui.theme.SecondaryCyan
import com.mad.myfitnesstrackingapp.ui.theme.TextFieldBackground
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

enum class ActivityType { CYCLING, RUNNING, WALKING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTrackScreen(
    navController: NavController,
    type: ActivityType,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    var isTracking by remember { mutableStateOf(false) }
    var durationSec by remember { mutableStateOf(0L) }
    var distanceMeters by remember { mutableStateOf(0.0) }
    var avgSpeed by remember { mutableStateOf(0.0) }

    // Demo timer & fake distance updates
    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (isTracking) {
                delay(1000)
                durationSec += 1
                val rate = when (type) {
                    ActivityType.CYCLING -> 6.0
                    ActivityType.RUNNING -> 3.0
                    ActivityType.WALKING -> 1.4
                }
                distanceMeters += rate
                avgSpeed = if (durationSec > 0) distanceMeters / durationSec else 0.0
            }
        }
    }

    val cardBg = Color.White.copy(alpha = 0.06f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() ?: navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },

        // ✅ Floating Start/Stop Button
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isTracking = !isTracking },
                text = { Text(if (isTracking) "Stop" else "Start") },
                icon = {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                containerColor = SecondaryCyan,
                contentColor = Color.White
            )
        },

        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GradientTop,
                            GradientMid,
                            GradientBottom
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Map placeholder


                // Map container using Maps Compose
// inside the Box where you had MapPlaceholder
// Map container using Maps Compose — responsive height using weight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.54f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        MapWithCurrentLocation(modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(durationMillis = 250)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg,
                        contentColor = Color.White
                    ),
                    //  elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Duration", value = formatDuration(durationSec))
                            StatItem(label = "Distance", value = formatDistance(distanceMeters))
                            StatItem(
                                label = if (type == ActivityType.CYCLING) "Avg speed" else "Pace",
                                value = if (type == ActivityType.CYCLING)
                                    formatSpeed(avgSpeed)
                                else formatPace(avgSpeed)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${(distanceMeters / 1000.0).format(2)} km • ${distanceMeters.roundToInt()} m",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Row {
                                IconButton(onClick = { /* Pause */ }, enabled = isTracking) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = PrimaryBlue
                                    )
                                }
                                IconButton(onClick = {
                                    isTracking = false
                                    durationSec = 0
                                    distanceMeters = 0.0
                                    avgSpeed = 0.0
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset",
                                        tint = PrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chips Row (Start removed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallInfoChip(
                        icon = Icons.Default.Timeline,
                        label = "Live path",
                        chipBg = TextFieldBackground
                    )
                    SmallInfoChip(
                        icon = Icons.Default.Whatshot,
                        label = when (type) {
                            ActivityType.CYCLING -> "High cadence"
                            ActivityType.RUNNING -> "Calories"
                            ActivityType.WALKING -> "Steps"
                        },
                        chipBg = TextFieldBackground
                    )
                }

                Spacer(modifier = Modifier.weight(0.02f))
            }
        }
    }
}

@Composable
private fun MapPlaceholder(activityType: ActivityType, isTracking: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = when (activityType) {
                ActivityType.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
                ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White.copy(alpha = 0.95f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (isTracking) "Tracking live — map will show path here"
            else "Map preview — add Google Map later",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
private fun SmallInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    chipBg: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(chipBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White)
    }
}

// --- Helpers ---
private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000)
        String.format(Locale.getDefault(), "%.2f km", meters / 1000.0)
    else "${meters.roundToInt()} m"
}

private fun formatSpeed(mps: Double): String =
    String.format(Locale.getDefault(), "%.1f m/s", mps)

private fun formatPace(mps: Double): String {
    if (mps <= 0.0) return "--:-- /km"
    val secPerKm = (1000.0 / mps).toInt()
    val min = secPerKm / 60
    val sec = secPerKm % 60
    return "%d:%02d /km".format(min, sec)
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
