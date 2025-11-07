package com.mad.myfitnesstrackingapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.auth.AuthViewModel
import com.mad.myfitnesstrackingapp.navigation.NavRoute
import com.mad.myfitnesstrackingapp.screens.ui.ModernTextField
import com.mad.myfitnesstrackingapp.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.mad.myfitnesstrackingapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginSuccess by authViewModel.loginSuccess.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorState by authViewModel.errorState.collectAsState()

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // React to login success
    // React to login success
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            // Show the toast and set a clear small autoDismiss duration
            val toastDuration = 1200L
            TopToastController.show(
                "Login successful — welcome back!",
                TopToastType.SUCCESS,
                autoDismissMs = toastDuration
            )

            // Wait until toast auto-dismisses so it is shown only in the login screen.
            delay(toastDuration + 120L) // slight buffer to ensure animation completes

            // Navigate after the toast has finished
            navController.navigate(NavRoute.HOME) {
                popUpTo(NavRoute.LOGIN) { inclusive = true }
            }

            // Reset auth flow state
            authViewModel.resetLoginFlow()
        }
    }


    // React to error state
    LaunchedEffect(errorState) {
        errorState?.let { (code, msg) ->
            val display = if (code > 0) "Error $code: $msg" else msg
            TopToastController.show(display, TopToastType.ERROR)
            authViewModel.clearError()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(GradientTop, GradientBottom)))
                .padding(paddingValues)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    focusManager.clearFocus()
                }
        ) {
            // Top toast host (placed under status bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarTopPadding + 6.dp)
                    .align(Alignment.TopCenter)
            ) {
                TopToastHost()
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome Back!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Login to continue your fitness journey", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(40.dp))

                ModernTextField(value = email, onValueChange = { email = it }, placeholder = "Email", enabled = !isLoading)
                Spacer(modifier = Modifier.height(20.dp))
                ModernTextField(value = password, onValueChange = { password = it }, placeholder = "Password", isPassword = true, enabled = !isLoading)
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            authViewModel.loginUser(email, password)
                        } else {
                            coroutineScope.launch {
                                TopToastController.show("Please fill in all fields.", TopToastType.INFO)
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                    } else {
                        Text("Login", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = { navController.navigate(NavRoute.REGISTER) }, enabled = !isLoading) {
                    Text("Don’t have an account? Register", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}
