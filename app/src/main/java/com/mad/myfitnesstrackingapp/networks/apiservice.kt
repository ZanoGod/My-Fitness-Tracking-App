package com.mad.myfitnesstrackingapp.networks

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // --- Authentication ---

    @POST("register.php") // Endpoint URL
    @FormUrlEncoded // Send data as form fields
    suspend fun register(
        @Field("username") username: String, // Matches $_POST['username']
        @Field("email") email: String,       // Matches $_POST['email']
        @Field("password") password: String   // Matches $_POST['password']
    ): Response<RegisterResponse> // The expected JSON response object

    @POST("login.php")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // --- Workouts ---

    @POST("add_activity.php")
    @FormUrlEncoded
    suspend fun addActivity(
        @Field("user_id") userId: Int,
        @Field("activity_type") activityType: String,
        @Field("duration_minutes") durationMinutes: Int,
        @Field("distance_km") distanceKm: String?,
        @Field("weight_kg") weightKg: String?,
        @Field("sets") sets: Int?,
        @Field("reps") reps: Int?
    ): Response<AddActivityResponse>


    @GET("get_activities.php") // This uses GET
    suspend fun getActivities(
        @Query("user_id") userId: Int // Matches $_GET['user_id']
    ): Response<ActivitiesResponse>

}
