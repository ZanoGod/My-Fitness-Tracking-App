package com.mad.myfitnesstrackingapp.db

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.mad.myfitnesstrackingapp.auth.VolleySingleton
import com.mad.myfitnesstrackingapp.networks.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * WorkoutDbViewModel - single source of truth for Dashboard & History.
 *
 * Expects server responses like:
 * { "status": "success", "activities": [ { "activity_id":1, "activity_type":"Running", "duration_minutes":30, "distance_km":"5.2", "activity_date":"2024-10-20 14:30:00" }, ... ] }
 */
@RequiresApi(Build.VERSION_CODES.O)
class WorkoutDbViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "WorkoutDbViewModel"
    private val REQUEST_TAG = "WorkoutRequests"
    private val GET_ACTIVITIES_URL = ApiConfig.GET_ACTIVITIES_URL
    private val ADD_ACTIVITY_URL = ApiConfig.ADD_ACTIVITY_URL

    // Flows exposed to UI
    private val _recentWorkouts = MutableStateFlow<List<WorkoutPreview>>(emptyList())
    val recentWorkouts = _recentWorkouts.asStateFlow()

    private val _history = MutableStateFlow<List<WorkoutPreview>>(emptyList())
    val history = _history.asStateFlow()

    private val _summary = MutableStateFlow(DashboardSummary(0, 0, 0.0))
    val summary = _summary.asStateFlow()

    private val _username = MutableStateFlow("User")
    val username = _username.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val queue = VolleySingleton.getQueue(application.applicationContext)

    // Formatter for server datetime strings "yyyy-MM-dd HH:mm:ss"
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    init {
        // load plain prefs immediately
        val prefs = application.getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
        _username.value = prefs.getString("username", "User") ?: "User"

        // Trigger initial load so Dashboard gets recent items without UI needing to call
        loadData(force = true)
    }

    /**
     * loadData - populates recentWorkouts + summary
     */
    fun loadData(limitRecent: Int = 5, force: Boolean = false) {
        viewModelScope.launch {
            if (_recentWorkouts.value.isNotEmpty() && !force) return@launch
            _isLoading.value = true
            fetchActivities { workouts ->
                val sorted = workouts.sortedByDescending { it.date }
                _recentWorkouts.value = sorted.take(limitRecent)
                _summary.value = calculateDashboardSummary(sorted)
                _isLoading.value = false
            }
        }
    }

    /**
     * loadHistory - populates history
     */
    fun loadHistory(force: Boolean = false) {
        viewModelScope.launch {
            if (_history.value.isNotEmpty() && !force) return@launch
            _isLoading.value = true
            fetchActivities { workouts ->
                val sorted = workouts.sortedByDescending { it.date }
                _history.value = sorted
                _isLoading.value = false
            }
        }
    }

    /**
     * addWorkout - POST to add_activity.php and refresh on success
     */
    fun addWorkout(
        type: String,
        duration: String,
        distance: String? = null,
        weight: String? = null,
        sets: String? = null,
        reps: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        onSuccess: () -> Boolean
    ) {
        if (type.isBlank() || duration.isBlank()) {
            Toast.makeText(getApplication(), "Activity type and duration are required", Toast.LENGTH_SHORT).show()
            return
        }

        val url = ADD_ACTIVITY_URL
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    Log.d(TAG, "ADD_ACTIVITY response: $response")
                    val json = JSONObject(response)
                    val status = json.optString("status", "error")
                    val message = json.optString("message", "")
                    if (status == "success") {
                        // refresh both lists
                        viewModelScope.launch {
                            loadData(force = true)
                            loadHistory(force = true)
                        }
                        try {
                            onSuccess()
                        } catch (e: Exception) {
                            Log.w(TAG, "onSuccess callback threw: ${e.message}")
                        }
                        Toast.makeText(getApplication(), "Workout saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(getApplication(), "Save failed: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing add activity response", e)
                    Toast.makeText(getApplication(), "Save failed (parse error)", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                val status = error.networkResponse?.statusCode ?: -1
                Log.e(TAG, "Volley add activity failed: code=$status msg=${error.message}")
                Toast.makeText(getApplication(), "Network error saving workout", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val prefs = getApplication<Application>().getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("user_id", -1)
                if (userId != -1) params["user_id"] = userId.toString()
                params["activity_type"] = type
                params["duration_minutes"] = duration
                if (!distance.isNullOrBlank()) params["distance_km"] = distance
                if (!weight.isNullOrBlank()) params["weight_kg"] = weight
                if (!sets.isNullOrBlank()) params["sets"] = sets
                if (!reps.isNullOrBlank()) params["reps"] = reps
                if (latitude != null) params["latitude"] = latitude.toString()
                if (longitude != null) params["longitude"] = longitude.toString()
                return params
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(10_000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.tag = REQUEST_TAG
        queue.add(stringRequest)
    }

    /**
     * logout - clear prefs and reset state
     */
    fun logout() {
        val prefs = getApplication<Application>().getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        _username.value = "User"
        _recentWorkouts.value = emptyList()
        _history.value = emptyList()
        _summary.value = DashboardSummary(0, 0, 0.0)
        _isOnline.value = true
        _isLoading.value = false

        Toast.makeText(getApplication(), "Logged out", Toast.LENGTH_SHORT).show()
    }

    /**
     * fetchActivities - GET activities, tries to include user_id query param if available
     *
     * This function avoids 'return' from inside the Volley lambda by using if/else flow.
     */
    private fun fetchActivities(onResult: (List<WorkoutPreview>) -> Unit) {
        // Append user_id query param if present
        val prefs = getApplication<Application>().getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        val url = if (userId != -1) {
            if (GET_ACTIVITIES_URL.contains("?")) "$GET_ACTIVITIES_URL&user_id=$userId" else "$GET_ACTIVITIES_URL?user_id=$userId"
        } else {
            GET_ACTIVITIES_URL
        }

        Log.d(TAG, "Fetching activities from: $url")

        val stringRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    Log.d(TAG, "GET_ACTIVITIES response: $response")
                    val json = JSONObject(response)
                    val status = json.optString("status", "error")
                    if (status != "success") {
                        Log.w(TAG, "Server returned non-success status: $status")
                        _isOnline.value = false
                        onResult(emptyList())
                    } else {
                        val activitiesArray = json.optJSONArray("activities")
                        if (activitiesArray == null) {
                            Log.w(TAG, "No activities array in response")
                            onResult(emptyList())
                        } else {
                            val results = mutableListOf<WorkoutPreview>()
                            for (i in 0 until activitiesArray.length()) {
                                val item = activitiesArray.getJSONObject(i)
                                val activityId = item.optInt("activity_id", -1)
                                val activityType = item.optString("activity_type", "Unknown")
                                val duration = item.optInt("duration_minutes", 0)
                                val distanceStr = if (item.has("distance_km") && !item.isNull("distance_km")) item.optString("distance_km") else null
                                val dateStr = item.optString("activity_date", null)
                                if (dateStr == null) {
                                    // skip entries without date
                                    continue
                                }
                                val date = try {
                                    LocalDateTime.parse(dateStr, formatter)
                                } catch (e: Exception) {
                                    Log.w(TAG, "date parse failed for [$dateStr], using now()", e)
                                    LocalDateTime.now()
                                }
                                val distanceKm = distanceStr?.toDoubleOrNull()
                                val preview = WorkoutPreview(
                                    id = activityId,
                                    type = activityType,
                                    durationMin = duration,
                                    distanceKm = distanceKm,
                                    date = date
                                )
                                results.add(preview)
                            }
                            _isOnline.value = true
                            onResult(results)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing activities JSON", e)
                    _isOnline.value = false
                    onResult(emptyList())
                }
            },
            { error ->
                val status = error.networkResponse?.statusCode ?: -1
                Log.e(TAG, "Volley get activities failed: code=$status msg=${error.message}")
                _isOnline.value = false
                onResult(emptyList())
            }
        ) {}

        stringRequest.retryPolicy = DefaultRetryPolicy(10_000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        stringRequest.tag = REQUEST_TAG
        queue.add(stringRequest)
    }

    override fun onCleared() {
        super.onCleared()
        queue.cancelAll(REQUEST_TAG)
    }


    fun addActivityToServer(
        application: Application,
        activityType: String,
        durationMinutes: Int,
        distanceKm: String? = null,
        weightKg: String? = null,
        sets: String? = null,
        reps: String? = null,
        onFinished: (success: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        // read user_id from plain prefs (same key used elsewhere)
        val plainPrefs = application.getSharedPreferences("FitnessAppPrefs", Context.MODE_PRIVATE)
        val userId = plainPrefs.getInt("user_id", -1)
        if (userId <= 0) {
            onFinished(false, "No logged-in user found.")
            return
        }

        val url = ApiConfig.ADD_ACTIVITY_URL // you can set this e.g. "http://192.168.0.29/fitness_api/add_activity.php"
        val queue = VolleySingleton.getQueue(application.applicationContext)

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    Log.d("WorkoutDbViewModel", "addActivity response: $response")
                    val json = JSONObject(response)
                    val status = json.optString("status", "error")
                    val message = json.optString("message", "")
                    if (status == "success") {
                        // Refresh local data after a successful remote insert
                        // make sure loadData() is safe to call from this thread
                        viewModelScope.launch {
                            loadData() // your existing method that refreshes summary/recentWorkouts
                        }
                        onFinished(true, message)
                    } else {
                        onFinished(false, message)
                    }
                } catch (e: Exception) {
                    Log.e("WorkoutDbViewModel", "parse error: ${e.message}")
                    onFinished(false, "Invalid server response")
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode ?: -1
                val body = error.networkResponse?.data?.let { String(it) }
                var msg = error.message ?: "Network error"
                if (body != null) {
                    try { msg = JSONObject(body).optString("message", msg) } catch (_: Exception) { }
                }
                Log.e("WorkoutDbViewModel", "Volley error: code=$code body=$body msg=${error.message}")
                onFinished(false, "Error $code: $msg")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["activity_type"] = activityType
                params["duration_minutes"] = durationMinutes.toString()
                params["distance_km"] = distanceKm ?: ""
                params["weight_kg"] = weightKg ?: ""
                params["sets"] = sets ?: ""
                params["reps"] = reps ?: ""
                return params
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10_000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        stringRequest.tag = "ActivityRequests"
        queue.add(stringRequest)
    }

}

/**
 * Local helper that calculates DashboardSummary from a list of WorkoutPreview.
 * Renamed to avoid conflicts with other extensions named `toDashboardSummary`.
 */
private fun calculateDashboardSummary(list: List<WorkoutPreview>): DashboardSummary {
    val totalWorkouts = list.size
    val totalMinutes = list.sumOf { it.durationMin }
    val totalDistance = list.sumOf { it.distanceKm ?: 0.0 }
    val totalDistanceKm = kotlin.math.round((totalDistance * 10.0)) / 10.0
    return DashboardSummary(totalWorkouts, totalMinutes, totalDistanceKm)
}


