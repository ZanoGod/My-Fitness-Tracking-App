package com.mad.myfitnesstrackingapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(onLogoutClick: () -> Unit) { // Added the onLogoutClick parameter
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Added padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to your Dashboard!", fontSize = 22.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(onClick = onLogoutClick) {
                Text("Logout")
            }
        }
    }
}

