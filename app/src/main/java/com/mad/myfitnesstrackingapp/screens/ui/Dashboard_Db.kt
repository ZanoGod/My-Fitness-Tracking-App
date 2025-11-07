package com.mad.myfitnesstrackingapp.screens.ui


import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime

data class WorkoutPreview(
    val id: Int,
    val type: String,
    val durationMin: Int,
    val distanceKm: Double?,
    val date: LocalDateTime
)

// --- Data Models for UI ---
data class Goal(
    val id: Int,
    val title: String,
    val progressPercent: Int // 0..100
)

data class QuickAction(
    val label: String,
    val icon: ImageVector? = null,
    val drawableRes: Int? = null,
    val onClick: () -> Unit
)
