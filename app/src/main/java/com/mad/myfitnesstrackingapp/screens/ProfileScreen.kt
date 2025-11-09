package com.mad.myfitnesstrackingapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.auth.AuthViewModel
import com.mad.myfitnesstrackingapp.db.WorkoutDbViewModel
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    workoutViewModel: WorkoutDbViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val username by workoutViewModel.username.collectAsState(initial = "")
    val email by authViewModel.email.collectAsState(initial = null)
    val createdAt by authViewModel.createdAt.collectAsState(initial = null)

    // If username empty, fallback to authViewModel's username
    val displayUsername = if (username.isNotBlank()) username else authViewModel.username.collectAsState(initial = "").value
    val displayEmail = email ?: "email@example.com"

    val joinedLabel by remember(createdAt) {
        mutableStateOf(
            createdAt?.let {
                try {
                    val parsed = LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    parsed.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                } catch (e: Exception) {
                    // fallback to raw if parse fails
                    it
                }
            } ?: "N/A"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GradientTop)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(GradientTop, GradientMid, GradientBottom)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(displayUsername.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(displayUsername, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text(displayEmail, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)

                Spacer(modifier = Modifier.height(32.dp))

                ProfileInfoCard(icon = painterResource(R.drawable.account_circle_24px), title = "Username", value = displayUsername)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoCard(icon = painterResource(R.drawable.email_24px), title = "Email", value = displayEmail)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInfoCard(icon = painterResource(R.drawable.calendar_month_24px), title = "Joined Date", value = joinedLabel)
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(icon: Painter, title: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
