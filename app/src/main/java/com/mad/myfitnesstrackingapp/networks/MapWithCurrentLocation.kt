package com.mad.myfitnesstrackingapp.networks


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission") // we handle permission at runtime
@Composable
fun MapWithCurrentLocation(
    modifier: Modifier = Modifier.fillMaxSize(),
    defaultCenter: LatLng = LatLng(37.4219999, -122.0840575),
    zoom: Float = 16f,
    updateIntervalMs: Long = 2000L
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled reactively below */ }

    // Track whether we have location permission
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Initial permission check
    LaunchedEffect(Unit) {
        val fine = context.checkSelfPermissionCompat(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = context.checkSelfPermissionCompat(Manifest.permission.ACCESS_COARSE_LOCATION)
        hasLocationPermission = fine || coarse
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    var pathPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            updateIntervalMs
        ).setMinUpdateIntervalMillis(updateIntervalMs / 2).build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val locations = result.locations
                if (locations.isNotEmpty()) {
                    val newPoints = locations.map { LatLng(it.latitude, it.longitude) }
                    pathPoints = pathPoints + newPoints
                }
            }
        }
    }

    // Start/stop location updates
    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedClient.lastLocation.addOnSuccessListener { last: Location? ->
                val target = last?.let { LatLng(it.latitude, it.longitude) } ?: defaultCenter
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, zoom))
                if (last != null) pathPoints = listOf(target)
            }
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        onDispose {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }

    // The Map
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        // Polyline for path
        if (pathPoints.size >= 2) {
            Polyline(points = pathPoints)
        }

        // ✅ Use MarkerState instead of position param
        pathPoints.lastOrNull()?.let { last ->
            Marker(
                state = rememberMarkerState(position = last),
                title = "Current location"
            )
        }
    }
}

/** Helper to check permissions safely */
private fun Context.checkSelfPermissionCompat(permission: String): Boolean =
    androidx.core.content.ContextCompat.checkSelfPermission(
        this,
        permission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
