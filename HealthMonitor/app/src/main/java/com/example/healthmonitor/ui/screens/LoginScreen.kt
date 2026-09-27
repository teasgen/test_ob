package com.example.healthmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmonitor.viewmodel.AuthState
import com.example.healthmonitor.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EBE7)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Вход в HealthHelp",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3D38))
                )
                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF48A999),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "или",
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF1E3D38).copy(alpha = 0.6f)),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF48A999),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF48A999),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val emailVal = email.ifEmpty { null }
                        val phoneVal = phone.ifEmpty { null }
                        if (emailVal != null || phoneVal != null) {
                            viewModel.login(emailVal, phoneVal, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48A999)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = password.isNotEmpty() && (email.isNotEmpty() || phone.isNotEmpty())
                ) {
                    Text("Войти", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text("Нет аккаунта? Зарегистрироваться", color = Color(0xFF48A999))
                }

                if (authState is AuthState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (authState as AuthState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                if (authState is AuthState.Loading) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color(0xFF48A999))
                }
            }
        }
    }
}