package com.mad.myfitnesstrackingapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.auth.AuthViewModel
import com.mad.myfitnesstrackingapp.navigation.NavRoute
import com.mad.myfitnesstrackingapp.screens.ui.ModernTextField
import com.mad.myfitnesstrackingapp.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val registerSuccess by authViewModel.registerSuccess.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorState by authViewModel.errorState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            TopToastController.show("✅ Registration successful!", TopToastType.SUCCESS)
            navController.navigate(NavRoute.LOGIN) {
                popUpTo(NavRoute.LOGIN) { inclusive = true }
            }
            authViewModel.resetRegisterFlow()
        }
    }

    LaunchedEffect(errorState) {
        errorState?.let { (code, msg) ->
            val display = if (code > 0) "⚠️ Error $code: $msg" else "⚠️ $msg"
            TopToastController.show(display, TopToastType.ERROR)
            authViewModel.clearError()
        }
    }

    Scaffold { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(GradientTop, GradientBottom)))
                .padding(paddingVals)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarTopPadding + 8.dp)
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
                Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Start your fitness journey today", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(40.dp))

                ModernTextField(value = name, onValueChange = { name = it }, placeholder = "Full Name")
                Spacer(modifier = Modifier.height(20.dp))
                ModernTextField(value = email, onValueChange = { email = it }, placeholder = "Email")
                Spacer(modifier = Modifier.height(20.dp))
                ModernTextField(value = password, onValueChange = { password = it }, placeholder = "Password", isPassword = true)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            authViewModel.registerUser(name, email, password)
                        } else {
                            coroutineScope.launch { TopToastController.show("⚠️ Please fill in all fields.", TopToastType.INFO) }
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
                        Text("Register", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = {
                    navController.navigate(NavRoute.LOGIN) {
                        popUpTo(NavRoute.LOGIN) { inclusive = true }
                    }
                }) {
                    Text("Already have an account? Login", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}
