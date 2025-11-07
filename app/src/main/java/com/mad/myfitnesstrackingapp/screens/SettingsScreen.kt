package com.mad.myfitnesstrackingapp.screens

import android.R.attr.onClick
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.painter.Painter // Import Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Import painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.R // Import R
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.ui.theme.LightCyan
import com.mad.myfitnesstrackingapp.ui.theme.PrimaryBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientTop // Match the gradient top
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
                    .verticalScroll(rememberScrollState()) // Make it scrollable
            ) {
                // --- Account Section ---
                SettingsGroupHeader("Account")
                SettingsClickableItem(
                    // Use the new R.drawable resource
                    icon = painterResource(id = R.drawable.person_pin_24px),
                    title = "Edit Profile",
                    subtitle = "Change your name, email, etc."
                ) { /* TODO: Navigate to Edit Profile */ }
                SettingsClickableItem(
                    icon = painterResource(id = R.drawable.settings_24px),
                    title = "Login & Security",
                    subtitle = "Manage your password"
                ) { /* TODO: Navigate to Change Password */ }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Preferences Section ---
                SettingsGroupHeader("Preferences")
                SettingsClickableItem(
                    icon = painterResource(id = R.drawable.weight_scale_24px),
                    title = "Units",
                    subtitle = "Metric (kg, km) / Imperial (lbs, mi)"
                ) { /* TODO: Show units dialog */ }

                // Mock state for toggles
                var notificationsOn by remember { mutableStateOf(true) }
                SettingsToggleItem(
                    icon = painterResource(id=R.drawable.edit_notifications),
                    title = "Push Notifications",
                    subtitle = "Workout reminders, goal updates",
                    isChecked = notificationsOn
                ) { notificationsOn = it }

                var darkModeOn by remember { mutableStateOf(true) }
                SettingsToggleItem(
                    icon = painterResource(id = R.drawable.dark_mode_24px),
                    title = "Dark Mode",
                    subtitle = "Sync with system",
                    isChecked = darkModeOn
                ) { darkModeOn = it }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Other Section ---
                SettingsGroupHeader("Other")
                SettingsClickableItem(
                    icon = painterResource(id = R.drawable.privacy_tip_24px),
                    title = "Privacy Policy",
                    subtitle = null
                ) { /* TODO: Open Privacy Policy URL */ }
                SettingsClickableItem(
                    icon = painterResource(id = R.drawable.description_24px),
                    title = "Terms of Service",
                    subtitle = null
                ) { /* TODO: Open Terms URL */ }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Delete Account Button ---
                TextButton(

                    onClick = {
                        Toast.makeText(context, " Delete Account!!!  ", Toast.LENGTH_SHORT)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(color = LightGray.copy(0.7f), shape = CircleShape)

                ) {
                    Text("Delete Account", color = Color.Red.copy(alpha = 0.7f), fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// --- NEW HELPER COMPOSABLES ---

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(Locale.getDefault()),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsClickableItem(
    icon: Painter,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // For now, just show a Toast as a placeholder
                Toast
                    .makeText(context, "$title Clicked (Not Implemented)", Toast.LENGTH_SHORT)
                    .show()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon, // Changed from imageVector to painter
            contentDescription = title,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(White)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Color.White)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: Painter, // Changed from ImageVector to Painter
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon, // Changed from imageVector to painter
            contentDescription = title,

            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

