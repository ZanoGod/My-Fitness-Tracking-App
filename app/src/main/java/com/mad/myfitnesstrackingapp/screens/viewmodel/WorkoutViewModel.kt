package com.mad.myfitnesstrackingapp.screens.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mad.myfitnesstrackingapp.db.ActivityNetworkResponse
import com.mad.myfitnesstrackingapp.db.WorkoutPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

// Data class for the summary cards
data class DashboardSummary(
    val totalWorkouts: Int,
    val totalMinutes: Int,
    val totalDistanceKm: Double
)


@RequiresApi(Build.VERSION_CODES.O)
fun ActivityNetworkResponse.toWorkoutPreview(): WorkoutPreview {
    // Define the expected date-time format from your API
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val parsedDate = try {
        LocalDateTime.parse(this.activity_date, formatter)
    } catch (e: DateTimeParseException) {
        Log.e("WorkoutPreview", "Failed to parse date: ${this.activity_date}")
        LocalDateTime.now() // Fallback to now
    }

    return WorkoutPreview(
        id = this.activity_id,
        type = this.activity_type,
        durationMin = this.duration_minutes,
        // Convert distance string to Double, or null if it's invalid/missing
        distanceKm = this.distance_km?.toDoubleOrNull(),
        date = parsedDate
    )
}

fun List<WorkoutPreview>.toDashboardSummary(): DashboardSummary {
    val totalWorkouts = this.size
    val totalMinutes = this.sumOf { it.durationMin }
    val totalDistance = this.sumOf { it.distanceKm ?: 0.0 }
    // Round to one decimal place
    val totalDistanceKm = (totalDistance * 10).roundToInt() / 10.0
    return DashboardSummary(totalWorkouts, totalMinutes, totalDistanceKm)
}
