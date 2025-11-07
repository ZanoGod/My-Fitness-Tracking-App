package com.mad.myfitnesstrackingapp.db

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class Workout_Db_connection(application: Application) : AndroidViewModel(application) {

    private val SERVER_IP = "192.168.0.75"
    private val GET_ACTIVITIES_URL = "http://$SERVER_IP/fitness_api/get_activities.php"
    private val ADD_ACTIVITY_URL = "http://$SERVER_IP/fitness_api/add_activity.php"

    private val queue = Volley.newRequestQueue(application.applicationContext)
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)

    // --- StateFlows for the UI ---
    private val _username = MutableStateFlow("User")
    val username = _username.asStateFlow()

    private val _recentWorkouts = MutableStateFlow<List<WorkoutPreview>>(emptyList())
    val recentWorkouts = _recentWorkouts.asStateFlow()

    private val _summary = MutableStateFlow(DashboardSummary(0, 0, 0.0))
    val summary = _summary.asStateFlow()

    private val _workoutAdded = MutableStateFlow<Boolean>(false)
    val workoutAdded = _workoutAdded.asStateFlow()

    init {
        // Load data as soon as the ViewModel is created
        loadData()
    }

    fun loadData() {
        loadUsername()
        fetchActivities()
    }

    private fun loadUsername() {
        val savedUsername = sharedPreferences.getString("username", "User")
        _username.value = savedUsername ?: "User"
    }

    private fun fetchActivities() {
        val userId = sharedPreferences.getInt("user_id", -1)

        // Handle offline demo user
        if (userId == -1 && _username.value == "User") {
            Log.d("WorkoutViewModel", "Loading demo data for offline user.")
            val demoWorkouts = getDemoWorkouts()
            _recentWorkouts.value = demoWorkouts
            _summary.value = demoWorkouts.toDashboardSummary()
            return // Don't make network call
        }

        if (userId == -1) {
            Log.e("WorkoutViewModel", "User ID not found, can't fetch activities.")
            return
        }


        val url = "$GET_ACTIVITIES_URL?user_id=$userId"
        Log.d("WorkoutViewModel", "Fetching from: $url")

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    Log.d("WorkoutViewModel", "Activities Response: $response")
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    if (status == "success") {
                        val activitiesArray = jsonResponse.getJSONArray("activities")
                        val allActivities = parseActivities(activitiesArray).sortedByDescending { it.date }

                        // Update the StateFlows
                        _recentWorkouts.value = allActivities.take(5) // Show 5 most recent
                        _summary.value = allActivities.toDashboardSummary()

                    } else {
                        Toast.makeText(getApplication(), "Failed to load activities", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("WorkoutViewModel", "Error parsing activities: ${e.message}")
                }
            },
            { error ->
                Log.e("WorkoutViewModel", "Volley Error: ${error.message}")
                Toast.makeText(getApplication(), "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(stringRequest)
    }

    // --- Demo Data ---
    private fun getDemoWorkouts(): List<WorkoutPreview> {
        val now = LocalDateTime.now()
        return listOf(
            WorkoutPreview(1, "Running", 40, 6.2, now.minusDays(0)),
            WorkoutPreview(2, "Cycling", 60, 20.4, now.minusDays(1)),
            WorkoutPreview(3, "Weightlifting", 50, null, now.minusDays(2)),
            WorkoutPreview(4, "Running", 35, 5.1, now.minusDays(4)),
        )
    }

    private fun parseActivities(activitiesArray: JSONArray): List<WorkoutPreview> {
        val list = mutableListOf<WorkoutPreview>()
        for (i in 0 until activitiesArray.length()) {
            try {
                val obj = activitiesArray.getJSONObject(i)
                val networkResponse = ActivityNetworkResponse(
                    activity_id = obj.getInt("activity_id"),
                    activity_type = obj.getString("activity_type"),
                    duration_minutes = obj.getInt("duration_minutes"),
                    distance_km = obj.optString("distance_km", null),
                    weight_kg = obj.optString("weight_kg", null),
                    sets = obj.optInt("sets", 0).let { if(it == 0) null else it },
                    reps = obj.optInt("reps", 0).let { if(it == 0) null else it },
                    activity_date = obj.getString("activity_date")
                )
                list.add(networkResponse.toWorkoutPreview())
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "Error parsing single activity: ${e.message}")
            }
        }
        return list
    }

    // --- THIS IS THE UPDATED FUNCTION ---
    fun addWorkout(
        type: String,
        duration: String,
        distance: String?,
        weight: String?,
        sets: String?,
        reps: String?,
        onSuccess: () -> Boolean // <-- PARAMETER ADDED HERE
    ) {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(getApplication(), "Error: Not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, ADD_ACTIVITY_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    if (status == "success") {
                        Toast.makeText(getApplication(), "Workout saved!", Toast.LENGTH_SHORT).show()
                        // Notify UI to navigate back
                        viewModelScope.launch { _workoutAdded.emit(true) }
                        onSuccess() // <-- CALLBACK CALLED HERE
                        // Refresh the dashboard data
                        fetchActivities()
                    } else {
                        Toast.makeText(getApplication(), "Failed to save: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("WorkoutViewModel", "Error parsing add response: ${e.message}")
                }
            },
            { error ->
                Log.e("WorkoutViewModel", "Volley Error: ${error.message}")
                Toast.makeText(getApplication(), "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["user_id"] = userId.toString()
                params["activity_type"] = type
                params["duration_minutes"] = duration

                // Add optional params only if they are not null or blank
                if (!distance.isNullOrBlank()) params["distance_km"] = distance
                if (!weight.isNullOrBlank()) params["weight_kg"] = weight
                if (!sets.isNullOrBlank()) params["sets"] = sets
                if (!reps.isNullOrBlank()) params["reps"] = reps

                return params
            }
        }
        queue.add(stringRequest)
    }

    fun resetWorkoutAddedFlow() {
        viewModelScope.launch {
            _workoutAdded.emit(false)
        }
    }

    fun logout() {
        viewModelScope.launch {
            with(sharedPreferences.edit()) {
                remove("user_id")
                remove("username")
                apply()
            }
            // Reset all local data
            _username.value = "User"
            _recentWorkouts.value = emptyList()
            _summary.value = DashboardSummary(0, 0, 0.0)
        }
    }
}