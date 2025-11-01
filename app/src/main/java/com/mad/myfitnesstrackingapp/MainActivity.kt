package com.mad.myfitnesstrackingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mad.myfitnesstrackingapp.ui.theme.FitnessAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                // Use Scaffold to get inner padding
                Navigation()
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    // Pass the innerPadding to your Navigation composable
//                    Navigation(
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
            }
        }
    }
}

