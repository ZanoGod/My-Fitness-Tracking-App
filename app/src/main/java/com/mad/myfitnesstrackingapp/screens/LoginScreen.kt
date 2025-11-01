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
import com.mad.myfitnesstrackingapp.screens.ui.ModernTextField
import com.mad.myfitnesstrackingapp.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterNavigate: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientTop, GradientBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome Back!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Login to continue your fitness journey",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            ModernTextField(value = email, onValueChange = { email = it }, placeholder = "Email")
            Spacer(modifier = Modifier.height(20.dp))
            ModernTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onLoginClick(email, password) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Login", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onRegisterNavigate) {
                Text("Don’t have an account? Register", color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}
