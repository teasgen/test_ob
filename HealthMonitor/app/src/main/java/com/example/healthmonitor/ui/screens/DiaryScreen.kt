package com.example.healthmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmonitor.data.DiaryRecord
import com.example.healthmonitor.viewmodel.MainViewModel
import com.example.healthmonitor.viewmodel.SendState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(state.sendState) {
        if (state.sendState is SendState.Success) {
            showForm = false
            kotlinx.coroutines.delay(2000)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Дневник здоровья", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Button(
                    onClick = { showForm = !showForm },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (showForm) "Скрыть форму" else "+ Новая запись",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (showForm) {
                item {
                    DiaryForm(
                        onDismiss = { showForm = false },
                        onSave = { scores ->
                            viewModel.saveDiaryRecord(
                                wellBeing = scores.wellBeing,
                                wellBeingScore = scores.wellBeingScore,
                                workCapacity = scores.workCapacity,
                                workCapacityScore = scores.workCapacityScore,
                                sleep = scores.sleep,
                                sleepScore = scores.sleepScore,
                                appetite = scores.appetite,
                                appetiteScore = scores.appetiteScore,
                                mood = scores.mood,
                                moodScore = scores.moodScore
                            )
                        },
                        isLoading = state.sendState is SendState.Loading
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            if (state.sendState is SendState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MintGreen)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (state.sendState is SendState.Error) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            " ${(state.sendState as SendState.Error).msg}",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (state.sendState is SendState.Success) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Запись отправлена врачу!",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Text(
                    "История записей",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.diaryRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Записей пока нет",
                            style = TextStyle(fontSize = 16.sp, color = DarkText.copy(alpha = 0.6f))
                        )
                    }
                }
            } else {
                items(state.diaryRecords) { record ->
                    DiaryCard(record)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DiaryCard(record: DiaryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightMint),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(record.timestamp)),
                style = TextStyle(fontSize = 14.sp, color = DarkText.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(12.dp))
            DiaryMetric("Самочувствие", record.wellBeingScore)
            DiaryMetric("Работоспособность", record.workCapacityScore)
            DiaryMetric("Сон", record.sleepScore)
            DiaryMetric("Аппетит", record.appetiteScore)
            DiaryMetric("Настроение", record.moodScore)
        }
    }
}

@Composable
fun DiaryMetric(label: String, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = TextStyle(fontSize = 14.sp, color = DarkText))
        Text(
            "$score/5",
            style = TextStyle(fontSize = 14.sp, color = MintGreen, fontWeight = FontWeight.Bold)
        )
    }
}

data class DiaryScores(
    val wellBeing: String = "",
    val wellBeingScore: Int = 3,
    val workCapacity: String = "",
    val workCapacityScore: Int = 3,
    val sleep: String = "",
    val sleepScore: Int = 3,
    val appetite: String = "",
    val appetiteScore: Int = 3,
    val mood: String = "",
    val moodScore: Int = 3
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryForm(
    onDismiss: () -> Unit,
    onSave: (DiaryScores) -> Unit,
    isLoading: Boolean
) {
    var scores by remember { mutableStateOf(DiaryScores()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Новая запись",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
            )
            Spacer(Modifier.height(16.dp))
            ScoreSlider("Самочувствие", scores.wellBeingScore) { newScore ->
                scores = scores.copy(wellBeingScore = newScore, wellBeing = getScoreLabel(newScore))
            }
            ScoreSlider("Работоспособность", scores.workCapacityScore) { newScore ->
                scores = scores.copy(workCapacityScore = newScore, workCapacity = getScoreLabel(newScore))
            }
            ScoreSlider("Сон", scores.sleepScore) { newScore ->
                scores = scores.copy(sleepScore = newScore, sleep = getScoreLabel(newScore))
            }
            ScoreSlider("Аппетит", scores.appetiteScore) { newScore ->
                scores = scores.copy(appetiteScore = newScore, appetite = getScoreLabel(newScore))
            }
            ScoreSlider("Настроение", scores.moodScore) { newScore ->
                scores = scores.copy(moodScore = newScore, mood = getScoreLabel(newScore))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Отмена", color = DarkText)
                }

                Button(
                    onClick = { onSave(scores) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = TextStyle(fontSize = 14.sp, color = DarkText))
            Text(
                "$value - ${getScoreLabel(value)}",
                style = TextStyle(fontSize = 14.sp, color = MintGreen, fontWeight = FontWeight.Bold)
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = MintGreen,
                activeTrackColor = MintGreen
            )
        )
    }
}

fun getScoreLabel(score: Int): String {
    return when (score) {
        1 -> "Очень плохо"
        2 -> "Плохо"
        3 -> "Нормально"
        4 -> "Хорошо"
        5 -> "Отлично"
        else -> "Нормально"
    }
}