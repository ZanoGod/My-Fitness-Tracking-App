package com.mad.myfitnesstrackingapp.screens.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@Composable
fun MapWithCurrentLocation(
    modifier: Modifier = Modifier.fillMaxSize(),
    cameraPositionState: CameraPositionState,
    lastLocationState: MutableState<LatLng?>,
    pathPointsState: MutableState<List<LatLng>>,
    defaultCenter: LatLng = LatLng(37.4219999, -122.0840575),
    zoom: Float = 16f,
    updateIntervalMs: Long = 2000L,
    autoFollow: Boolean = false           // <-- new parameter (auto-move only when true)
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled reactively below */ }

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
                    pathPointsState.value = pathPointsState.value + newPoints
                    val last = newPoints.last()
                    lastLocationState.value = last

                    // ✅ Move camera only when autoFollow is true
                    if (autoFollow) {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(last, zoom))
                    }
                }
            }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedClient.lastLocation.addOnSuccessListener { last: Location? ->
                val target = last?.let { LatLng(it.latitude, it.longitude) } ?: defaultCenter
                if (autoFollow) {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, zoom))
                }
                last?.let {
                    val alsoPoint = LatLng(it.latitude, it.longitude)
                    pathPointsState.value = pathPointsState.value + listOf(alsoPoint)
                    lastLocationState.value = alsoPoint
                }
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

    // Map UI
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        if (pathPointsState.value.size >= 2) {
            Polyline(points = pathPointsState.value)
        }
        lastLocationState.value?.let { last ->
            Marker(
                state = rememberMarkerState(position = last),
                title = "Current location"
            )
        }
    }
}

private fun Context.checkSelfPermissionCompat(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
