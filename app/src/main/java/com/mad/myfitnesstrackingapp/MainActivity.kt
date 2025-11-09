package com.mad.myfitnesstrackingapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mad.myfitnesstrackingapp.navigation.Navigation
import com.mad.myfitnesstrackingapp.notifications.NotificationChannels
import com.mad.myfitnesstrackingapp.ui.theme.FitnessAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        //
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen: SplashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Create notification channels at startup
        NotificationChannels.createAllChannels(applicationContext)

        enableEdgeToEdge()

        // Use a mutable boolean that the splash API will poll
        var keepSplashOn = true
        splashScreen.setKeepOnScreenCondition { keepSplashOn }

        // Request location permissions
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // then dismiss splash
        lifecycleScope.launch {
            delay(1000L)
            keepSplashOn = false
        }


        setContent {
            FitnessAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
