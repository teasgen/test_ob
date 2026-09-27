package com.example.healthmonitor.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmonitor.data.auth.AuthManager
import com.example.healthmonitor.viewmodel.MainViewModel

val MintGreen = Color(0xFF48A999)
val LightMint = Color(0xFFD4EBE7)
val DarkText = Color(0xFF1E3D38)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDiary: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToDoctorRecords: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val inviteCode = AuthManager.getInstance().inviteCode ?: "------"
    var copied by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                // Верхняя панель с кодом и кнопкой выхода
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "HealthHelp",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(inviteCode, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Red.copy(alpha = 0.1f),
                                contentColor = Color.Red
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Выйти", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Карточки с показателями
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VitalCard(
                        "Движение",
                        "${state.vitals.lastOrNull()?.steps ?: 0}",
                        "шагов",
                        modifier = Modifier.weight(1f)
                    )
                    VitalCard(
                        "Пульс",
                        "${state.vitals.lastOrNull()?.heartRate ?: 0}",
                        "уд/мин",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VitalCard(
                        "Температура",
                        String.format("%.1f", state.vitals.lastOrNull()?.temperature ?: 36.6f),
                        "°C",
                        modifier = Modifier.weight(1f)
                    )
                    VitalCard(
                        "SpO2",
                        "${state.vitals.lastOrNull()?.spO2 ?: 0}",
                        "%",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VitalCard(
                        "Сон",
                        state.vitals.lastOrNull()?.sleepTime ?: "—",
                        "",
                        modifier = Modifier.weight(1f)
                    )
                    VitalCard(
                        "Оценка сна",
                        "${state.vitals.lastOrNull()?.sleepScore ?: 0}",
                        "баллов",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VitalCard(
                        "Стресс",
                        "${state.vitals.lastOrNull()?.stressLevel ?: 0}",
                        "баллов",
                        modifier = Modifier.weight(1f)
                    )
                    // Пустая карточка для симметрии
                    Card(
                        modifier = Modifier.weight(1f).height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = LightMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "—",
                                style = TextStyle(fontSize = 28.sp, color = DarkText.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Кнопка генерации мок-данных
            item {
                Button(
                    onClick = { viewModel.refreshMockData() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(" Обновить данные", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))

                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(20.dp))
            }

            // Кнопки навигации
            item {
                NavigationButton("Дневник здоровья", onNavigateToDiary)
                Spacer(Modifier.height(12.dp))
                NavigationButton("Документы", onNavigateToDocuments)
                Spacer(Modifier.height(12.dp))
                NavigationButton("Записи врача", onNavigateToDoctorRecords)
                Spacer(Modifier.height(20.dp))

                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(20.dp))
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun VitalCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = LightMint),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = TextStyle(fontSize = 14.sp, color = DarkText.copy(alpha = 0.7f)))
            Spacer(Modifier.height(4.dp))
            Text(value, style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText))
            if (unit.isNotEmpty()) {
                Text(unit, style = TextStyle(fontSize = 12.sp, color = DarkText.copy(alpha = 0.7f)))
            }
        }
    }
}

@Composable
fun NavigationButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}