package com.mad.myfitnesstrackingapp.db


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// This data comes from the get_activities.php script
data class ActivityNetworkResponse(
    val activity_id: Int,
    val activity_type: String,
    val duration_minutes: Int,
    val distance_km: String?, // Comes as string from JSON, can be null
    val weight_kg: String?,   // Comes as string from JSON, can be null
    val sets: Int?,          // Comes as Int from JSON, can be null
    val reps: Int?,          // Comes as Int from JSON, can be null
    val activity_date: String // e.g., "2024-10-20 14:30:00"
)

// This is the model the UI (DashboardScreen) will use
data class WorkoutPreview(
    val id: Int,
    val type: String,
    val durationMin: Int,
    val distanceKm: Double?,
    val date: LocalDateTime
)

// Summary data calculated from the list of activities
data class DashboardSummary(
    val totalWorkouts: Int,
    val totalMinutes: Int,
    val totalDistanceKm: Double
)

// Helper function to convert the network response to the UI model
@RequiresApi(Build.VERSION_CODES.O)
fun ActivityNetworkResponse.toWorkoutPreview(): WorkoutPreview {
    // Define the format your PHP script's NOW() function returns
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return WorkoutPreview(
        id = this.activity_id,
        type = this.activity_type,
        durationMin = this.duration_minutes,
        distanceKm = this.distance_km?.toDoubleOrNull(),
        date = LocalDateTime.parse(this.activity_date, formatter)
    )
}

// Helper function to calculate the summary
fun List<WorkoutPreview>.toDashboardSummary(): DashboardSummary {
    val totalWorkouts = this.size
    val totalMinutes = this.sumOf { it.durationMin }
    val totalDistance = this.sumOf { it.distanceKm ?: 0.0 }

    // Round distance to one decimal place
    val totalDistanceKm = (totalDistance * 10).roundToInt() / 10.0

    return DashboardSummary(totalWorkouts, totalMinutes, totalDistanceKm)
}
