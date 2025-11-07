package com.mad.myfitnesstrackingapp.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextPainter.paint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.auth.AuthViewModel
import com.mad.myfitnesstrackingapp.db.Workout_Db_connection
import com.mad.myfitnesstrackingapp.screens.ProfileInfoCard
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    workoutViewModel: Workout_Db_connection // <-- 1. ACCEPT THE VIEWMODEL
) {

    // --- 2. GET THE REAL USERNAME FROM THE VIEWMODEL ---
    val username = workoutViewModel.username.collectAsState(initial = "").value

    // --- Demo User Data (like from PHP/ViewModel) ---
    // TODO: Pass in an AuthViewModel to get the real email and join date
    // For now, we will use the real username and keep demo data for the rest.
    val userProfile = mapOf(
        "username" to username, // <-- 3. USE THE REAL USERNAME
        "email" to "alice@example.com", // (This should come from AuthViewModel)
        "joined" to "Oct 28, 2024"      // (This should come from AuthViewModel)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientTop
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(listOf(GradientTop, GradientMid, GradientBottom))
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Big Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userProfile["username"]?.take(1)?.uppercase() ?: "U", // Uses real username
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Username
                Text(
                    userProfile["username"] ?: "User", // Uses real username
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                // Email
                Text(
                    userProfile["email"] ?: "email@example.com",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- Info Cards ---
                ProfileInfoCard(
                    icon = painterResource(R.drawable.account_circle_24px),
                    title = "Username",
                    value = userProfile["username"] ?: "User" // Uses real username
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoCard(
                    icon = painterResource(R.drawable.email_24px),
                    title = "Email",
                    value = userProfile["email"] ?: "email@example.com"
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoCard(
                    icon = painterResource(R.drawable.calendar_month_24px), // Changed icon
                    title = "Joined Date",
                    value = userProfile["joined"] ?: "N/A"
                )
            }
        }
    }
}

// Helper composable for this screen
@Composable
private fun ProfileInfoCard(   icon: Painter,  title: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
