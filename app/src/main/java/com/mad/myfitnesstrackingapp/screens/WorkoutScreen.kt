package com.mad.myfitnesstrackingapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.mad.myfitnesstrackingapp.db.WorkoutDbViewModel
import com.mad.myfitnesstrackingapp.screens.ui.ModernTextField
import com.mad.myfitnesstrackingapp.ui.theme.AccentBlue
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.ui.theme.PrimaryBlue
import com.mad.myfitnesstrackingapp.ui.theme.TextFieldBackground
import com.mad.myfitnesstrackingapp.notifications.NotificationHelper
import com.mad.myfitnesstrackingapp.notifications.NotificationChannels

// List of available activity types
val allActivityTypes = listOf(
    "Running",
    "Walking",
    "Cycling",
    "Weightlifting",
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class) // Added for ExposedDropdownMenuBox
@Composable
fun ManualAddWorkoutScreen(
    addWorkout: WorkoutDbViewModel,
    onWorkoutSaved: () -> Boolean
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    // --- UPDATED STATE ---
    var selectedActivity by remember { mutableStateOf("") }
    var isActivityDropdownExpanded by remember { mutableStateOf(false) }
    // --- END OF UPDATE ---

    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    // State for location
    var latitude by remember { mutableStateOf<Double?>(0.0) }
    var longitude by remember { mutableStateOf<Double?>(0.0) }
    var locationText by remember { mutableStateOf("No location yet") }

    // --- GEOLOCATION LOGIC (Task 4b) ---
    val locationClient = LocationServices.getFusedLocationProviderClient(context)

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                // Permission granted, get location
                fetchLocation(context) { lat, lon ->
                    latitude = lat
                    longitude = lon
                    locationText = "Location captured!"
                }
            } else {
                // Permission denied
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                locationText = "Permission denied"
            }
        }
    )
    // --- END OF GEOLOCATION LOGIC ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientTop, GradientBottom)
                )
            ).clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // Make column scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                "Log New Workout",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(40.dp))

            // --- REPLACED MULTI-SELECT LIST WITH DROPDOWN ---
            Text(
                "Select Activity",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = isActivityDropdownExpanded,
                onExpandedChange = { isActivityDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedActivity,
                    onValueChange = {}, // Not editable directly
                    readOnly = true,
                    placeholder = { Text("Select an Activity", color = Color.White.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isActivityDropdownExpanded)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        focusedContainerColor = TextFieldBackground,
                        unfocusedContainerColor = TextFieldBackground,
                        cursorColor = PrimaryBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        focusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                        disabledBorderColor = Color.White.copy(alpha = 0.3f),
                        disabledContainerColor = TextFieldBackground.copy(alpha = 0.5f),
                        disabledTextColor = Color.White.copy(alpha = 0.7f),
                        disabledPlaceholderColor = Color.White.copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = isActivityDropdownExpanded,
                    onDismissRequest = { isActivityDropdownExpanded = false },
                    modifier = Modifier
                        .background(
                            color = GradientBottom
                        )
                        .fillMaxWidth(0.85f)
                ) {
                    allActivityTypes.forEach { activity ->
                        DropdownMenuItem(
                            text = { Text(activity, color = Color.White, textAlign = TextAlign.Center) },
                            onClick = {
                                selectedActivity = activity
                                isActivityDropdownExpanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // --- END OF REPLACEMENT ---

            // Other Form Fields
            ModernTextField(
                value = duration,
                onValueChange = { duration = it },
                placeholder = "Duration (in minutes)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ModernTextField(
                value = distance,
                onValueChange = { distance = it },
                placeholder = "Distance (km) - Optional",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ModernTextField(
                value = weight,
                onValueChange = { weight = it },
                placeholder = "Weight (kg) - Optional",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ModernTextField(
                value = sets,
                onValueChange = { sets = it },
                placeholder = "Sets - Optional",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ModernTextField(
                value = reps,
                onValueChange = { reps = it },
                placeholder = "Reps - Optional",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Geolocation Button
            Button(
                onClick = {
                    // Check for permission and launch if needed
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fetchLocation(context) { lat, lon ->
                            latitude = lat
                            longitude = lon
                            locationText = "Location captured!"
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Get Current Location",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(locationText, color = Color.White.copy(alpha = 0.8f))
            Text("$latitude & $longitude", color = Color.White.copy(alpha = 0.8f))

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    // Validate required fields
                    if (selectedActivity.isNotBlank() && duration.isNotBlank()) {
                        // Build an onSuccess wrapper that calls your original callback
                        // and posts a notification when it reports success.
                        val onSuccessWrapper: () -> Boolean = {
                            val saved = onWorkoutSaved()
                            if (saved) {
                                // Post a notification (respecting Android 13+ runtime permission)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        NotificationHelper.postNotification(
                                            context = context,
                                            channelId = NotificationChannels.CHANNEL_REMINDERS,
                                            title = "Workout saved",
                                            message = "$selectedActivity for $duration minutes logged!"
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Notification permission not granted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    // pre-Android 13: post without runtime permission
                                    NotificationHelper.postNotification(
                                        context = context,
                                        channelId = NotificationChannels.CHANNEL_REMINDERS,
                                        title = "Workout saved",
                                        message = "$selectedActivity for $duration minutes logged!"
                                    )
                                }
                            }
                            saved
                        }

                        // Call addWorkout with the wrapped callback
                        addWorkout.addWorkout(
                            type = selectedActivity,
                            duration = duration,
                            distance = distance.ifBlank { null },
                            weight = weight.ifBlank { null },
                            sets = sets.ifBlank { null },
                            reps = reps.ifBlank { null },
                            onSuccess = onSuccessWrapper
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Please select an Activity and fill in Duration",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Save Workout",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Helper function for Geolocation
@SuppressLint("MissingPermission")
private fun fetchLocation(
    context: Context,
    onLocationFetched: (Double, Double) -> Unit
) {
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    locationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationFetched(location.latitude, location.longitude)
                Toast.makeText(context, "Location success!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error getting location: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}
