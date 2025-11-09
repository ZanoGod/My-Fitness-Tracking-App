package com.mad.myfitnesstrackingapp.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.R
import com.mad.myfitnesstrackingapp.notifications.NotificationChannels
import com.mad.myfitnesstrackingapp.notifications.NotificationHelper
import com.mad.myfitnesstrackingapp.notifications.NotificationPrefs
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientMid
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.ui.theme.LightCyan
import com.mad.myfitnesstrackingapp.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Flow from DataStore (NotificationPrefs.notificationsEnabledFlow)
    val notificationsEnabledFlow = NotificationPrefs.notificationsEnabledFlow(context)
    val notificationsOn by notificationsEnabledFlow.collectAsState(initial = true)

    // Permission launcher for Android 13+ POST_NOTIFICATIONS
    val shouldRequestPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                // Optionally show a toast when user grants
                Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // If denied, user might need to enable from settings
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.White) },
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
            ) {
                // --- Account Section ---
                SettingsGroupHeader("Account")
                SettingsClickableItem(
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

                // Push Notifications toggle: persistent + permission + test notification
                SettingsToggleItem(
                    icon = painterResource(id = R.drawable.edit_notifications),
                    title = "Push Notifications",
                    subtitle = "Workout reminders, goal updates",
                    isChecked = notificationsOn
                ) { newValue ->
                    scope.launch {
                        // Save new preference
                        NotificationPrefs.setNotificationsEnabled(context, newValue)

                        if (newValue) {
                            // If Android 13+, request runtime permission
                            if (shouldRequestPermission) {
                                val granted = checkNotificationPermission(context)
                                if (!granted) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }

                            // Post a test notification (best-effort)
                            NotificationHelper.postNotification(
                                context = context,
                                channelId = NotificationChannels.CHANNEL_REMINDERS,
                                title = "Notifications enabled",
                                message = "You'll now receive workout reminders."
                            )
                            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Dark mode toggle (example)
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
                ) {
                    // Example: open privacy policy URL
                    openUrl(context, "https://example.com/privacy")
                }
                SettingsClickableItem(
                    icon = painterResource(id = R.drawable.description_24px),
                    title = "Terms of Service",
                    subtitle = null
                ) {
                    openUrl(context, "https://example.com/terms")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Delete Account (example)
                TextButton(
                    onClick = {
                        Toast.makeText(context, "Delete Account!!!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(color = LightCyan.copy(alpha = 0.1f), shape = CircleShape)
                ) {
                    Text("Delete Account", color = Color.Red.copy(alpha = 0.9f), fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/* -------------------------
   Helper composables
   ------------------------- */

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
                Toast
                    .makeText(context, "$title Clicked (Not Implemented)", Toast.LENGTH_SHORT)
                    .show()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
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
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: Painter,
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
            painter = icon,
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

/* -------------------------
   Utility functions
   ------------------------- */

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}

@Suppress("UNUSED_PARAMETER")
private fun openAppNotificationSettings(context: Context) {
    // Useful if you want to direct the user to app notification settings
    try {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: open app details
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Cannot open app settings", Toast.LENGTH_SHORT).show()
        }
    }
}
