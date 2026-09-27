package com.example.healthmonitor.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmonitor.data.UploadedDocument
import com.example.healthmonitor.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTypeDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            selectedName = getFileNameFromUri(context, uri) ?: "document"
            showTypeDialog = true
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
                        Text("Документы", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isUploadingFile
                ) {
                    if (state.isUploadingFile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Загрузка...", color = Color.White, fontSize = 16.sp)
                    } else {
                        Text("+ Загрузить документ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))

                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text(
                    "Мои документы",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.uploadedDocuments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Документов пока нет",
                            style = TextStyle(fontSize = 16.sp, color = DarkText.copy(alpha = 0.6f))
                        )
                    }
                }
            } else {
                items(state.uploadedDocuments) { doc ->
                    DocumentCard(doc)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showTypeDialog && selectedUri != null) {
        DocumentTypeDialog(
            fileName = selectedName,
            onDismiss = {
                showTypeDialog = false
                selectedUri = null
            },
            onSelect = { type ->
                showTypeDialog = false
                viewModel.addDocument(context, type, selectedName, selectedUri!!)
                selectedUri = null
            }
        )
    }
}

@Composable
fun DocumentCard(doc: UploadedDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightMint),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MintGreen, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("", fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    doc.fileName,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText),
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    doc.type,
                    style = TextStyle(fontSize = 14.sp, color = DarkText.copy(alpha = 0.7f))
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    doc.uploadDate,
                    style = TextStyle(fontSize = 12.sp, color = DarkText.copy(alpha = 0.5f))
                )
            }

            Text(
                "",
                fontSize = 20.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun DocumentTypeDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Выберите тип документа", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Файл: $fileName", style = TextStyle(fontSize = 14.sp, color = Color.Gray))
                Spacer(Modifier.height(16.dp))

                val types = listOf("Анализы", "Результаты обследования", "Заключение врача", "Прочее")
                types.forEach { type ->
                    Button(
                        onClick = { onSelect(type) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(type, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.Gray)
            }
        }
    )

}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }
    } catch (e: Exception) {
    }

    if (name == null) {
        name = uri.lastPathSegment?.substringAfterLast('/')
    }

    return name
}