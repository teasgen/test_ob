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
import com.example.healthmonitor.viewmodel.DoctorNote
import com.example.healthmonitor.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorRecordsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
                        Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Записи врача", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    "Записи от врачей",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.doctorNotes.isEmpty()) {
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
                items(state.doctorNotes) { note ->
                    DoctorRecordItem(note)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DoctorRecordItem(note: DoctorNote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightMint),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$ {note.doctorName}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                )
                Text(
                    note.date,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = DarkText.copy(alpha = 0.6f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "На основе: ${note.reason}",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = DarkText.copy(alpha = 0.7f)
                )
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    note.description,
                    style = TextStyle(fontSize = 14.sp, color = DarkText, lineHeight = 20.sp),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}