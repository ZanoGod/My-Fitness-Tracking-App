package com.mad.myfitnesstrackingapp.networks

import com.google.gson.annotations.SerializedName

// --- Models for login.php ---

// This matches the 'user' object inside your login.php JSON
data class User(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String
)

// This matches the top-level response from login.php
data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User? // Nullable in case login fails
)

// --- Model for register.php ---
// This matches the response from register.php
data class RegisterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// --- Models for get_activities.php ---

// This matches one item in the 'activities' array
data class ActivityItem(
    @SerializedName("activity_id") val activityId: Int,
    @SerializedName("activity_type") val activityType: String,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("distance_km") val distanceKm: String?, // Keep as String? to handle NULL
    @SerializedName("weight_kg") val weightKg: String?,
    @SerializedName("sets") val sets: Int?,
    @SerializedName("reps") val reps: Int?,
    @SerializedName("activity_date") val activityDate: String
)

// This matches the top-level response from get_activities.php
data class ActivitiesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("activities") val activities: List<ActivityItem>
)

// --- Model for add_activity.php ---
// This matches the response from add_activity.php
data class AddActivityResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
