package com.mad.myfitnesstrackingapp.screens.workouts

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.mad.myfitnesstrackingapp.db.WorkoutDbViewModel
import com.mad.myfitnesstrackingapp.notifications.NotificationChannels
import com.mad.myfitnesstrackingapp.notifications.NotificationHelper
import com.mad.myfitnesstrackingapp.screens.viewmodel.MapWithCurrentLocation
import com.mad.myfitnesstrackingapp.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.*

enum class ActivityType { CYCLING, RUNNING, WALKING }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTrackScreen(
    navController: NavController,
    type: ActivityType,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,

    workoutViewModel: WorkoutDbViewModel = viewModel()

) {
    val context = LocalContext.current
    var isTracking by remember { mutableStateOf(false) }
    var durationSec by remember { mutableStateOf(0L) }
    var distanceMeters by remember { mutableStateOf(0.0) }
    var avgSpeed by remember { mutableStateOf(0.0) }


    val cameraPositionState = rememberCameraPositionState()
    val lastLocationState = remember { mutableStateOf<LatLng?>(null) }
    val pathPointsState = remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val cardBg = Color.White.copy(alpha = 0.06f)

    // Timer while tracking
    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (isTracking) {
                delay(1000)
                durationSec += 1
                distanceMeters = computeTotalDistanceMeters(pathPointsState.value)
                avgSpeed = if (durationSec > 0) distanceMeters / durationSec else 0.0
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },

        // Floating Start/Stop Button
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
//                    if (!isTracking) {
//                        // START tracking
//                        durationSec = 0L
//                        distanceMeters = 0.0
//                        avgSpeed = 0.0
//                        pathPointsState.value = emptyList()
//                        NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_DEFAULT, "Tracking", "Tracking started!")
//                    }
                    // STOP tracking branch (inside onClick)
                    if (isTracking) {
                        // compute values
                        val durationMinutes = (durationSec / 60).toInt().coerceAtLeast(1) // at least 1 minute
                        val distanceKmStr = String.format(Locale.getDefault(), "%.2f", distanceMeters / 1000.0)

                        // show temporary notification
                        NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_REMINDERS, "Saving", "Saving activity...")

                        // Call ViewModel to add activity to server
                        workoutViewModel.addActivityToServer(
                            application = context.applicationContext as Application,
                            activityType = when (type) {
                                ActivityType.CYCLING -> "Cycling"
                                ActivityType.RUNNING -> "Running"
                                ActivityType.WALKING -> "Walking"
                            },
                            durationMinutes = durationMinutes,
                            distanceKm = if (distanceMeters > 0.0) distanceKmStr else null,
                            weightKg = null,
                            sets = null,
                            reps = null
                        ) { success, message ->
                            // run on UI thread via LaunchedEffect or directly; replace toasts with notifications
                            if (success) {
                                NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_REMINDERS, "Saved", "Activity saved")
                            } else {
                                NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_REMINDERS, "Save failed", "Save failed: $message")
                            }
                        }

                        // present final stop message (optional)
                        val msg = "Stopped: ${formatDuration(durationSec)} • ${(distanceMeters / 1000.0).format(2)} km • Avg ${formatSpeed(avgSpeed)}"
                        NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_REMINDERS, "Stopped", msg)
                    }

                    else {
                        // STOP tracking (was not previously tracking)
                        val msg =
                            "Started: ${formatDuration(durationSec)} • ${(distanceMeters / 1000.0).format(2)} km • Avg ${formatSpeed(avgSpeed)}"
                        NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_REMINDERS, "Started", msg)
                    }
                    isTracking = !isTracking
                },
                text = { Text(if (isTracking) "Stop" else "Start") },
                icon = {
                    Icon(
                        if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                containerColor = SecondaryCyan
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
                        listOf(GradientTop, GradientMid, GradientBottom)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // --- MAP SECTION ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.54f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        MapWithCurrentLocation(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            lastLocationState = lastLocationState,
                            pathPointsState = pathPointsState,
                            zoom = 16f,
                            autoFollow = isTracking     // ✅ move camera only while tracking
                        )

                        // Manual center button
                        FloatingActionButton(
                            onClick = {
                                lastLocationState.value?.let { latLng ->
                                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                                } ?: NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_DEFAULT, "Location", "No location yet")
                            },
                            containerColor = SecondaryCyan,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Center map",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- STATS CARD ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(tween(250)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Duration", formatDuration(durationSec))
                            StatItem("Distance", formatDistance(distanceMeters))
                            StatItem(
                                if (type == ActivityType.CYCLING) "Avg speed" else "Pace",
                                if (type == ActivityType.CYCLING) formatSpeed(avgSpeed)
                                else formatPace(avgSpeed)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Distance text + control buttons
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
                                IconButton(onClick = { /* Pause (future) */ }, enabled = isTracking) {
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
                                    pathPointsState.value = emptyList()
                                    NotificationHelper.postNotification(context, NotificationChannels.CHANNEL_DEFAULT, "Tracking", "Tracking reset")
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

                // --- INFO CHIPS ---
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

/** Haversine distance between adjacent points, meters */
private fun computeTotalDistanceMeters(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var total = 0.0
    for (i in 0 until points.size - 1) {
        val a = points[i]
        val b = points[i + 1]
        total += haversineMeters(a.latitude, a.longitude, b.latitude, b.longitude)
    }
    return total
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val rlat1 = Math.toRadians(lat1)
    val rlat2 = Math.toRadians(lat2)
    val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(rlat1) * cos(rlat2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}
