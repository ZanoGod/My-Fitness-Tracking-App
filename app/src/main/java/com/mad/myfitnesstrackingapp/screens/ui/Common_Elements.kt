package com.mad.myfitnesstrackingapp.screens.ui


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mad.myfitnesstrackingapp.ui.theme.PrimaryBlue
import com.mad.myfitnesstrackingapp.ui.theme.TextFieldBackground

// This is your ModernTextField, moved to a common file so both screens can use it
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.7f)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedContainerColor = TextFieldBackground,
            unfocusedContainerColor = TextFieldBackground,
            cursorColor = PrimaryBlue,
            focusedTextColor = Color.White, // Set text color for focused state
            unfocusedTextColor = Color.White // Set text color for unfocused state
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
