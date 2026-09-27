package com.example.healthmonitor.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmonitor.data.*
import com.example.healthmonitor.data.auth.AuthManager
import com.example.healthmonitor.data.local.AppDatabase
import com.example.healthmonitor.data.mock.MockGenerator
import com.example.healthmonitor.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

sealed class SendState {
    object Idle : SendState()
    object Loading : SendState()
    object Success : SendState()
    data class Error(val msg: String) : SendState()
}

data class DoctorNote(
    val id: Int,
    val doctorName: String,
    val date: String,
    val description: String,
    val reason: String
)

data class UiState(
    val vitals: List<VitalRecord> = emptyList(),
    val diaryRecords: List<DiaryRecord> = emptyList(),
    val uploadedDocuments: List<UploadedDocument> = emptyList(),
    val doctorNotes: List<DoctorNote> = emptyList(),
    val sendState: SendState = SendState.Idle,
    val isUploadingFile: Boolean = false
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    private val authManager = AuthManager.getInstance()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadVitalsFromLocal()
        loadDiariesFromServer()
        loadDocumentsFromServer()
        loadDoctorNotesFromServer()
    }

    private fun getPatientId(): Int? = authManager.patientId

    private fun loadDiariesFromServer() {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch

            try {
                val response = NetworkModule.api.getDiaries(patientId)
                if (response.isSuccessful) {
                    val diaries = response.body()?.map { diaryResponse ->
                        DiaryRecord(
                            id = diaryResponse.id,
                            patientId = patientId,
                            timestamp = parseDate(diaryResponse.diary_date),
                            wellBeing = "Хорошее",
                            wellBeingScore = diaryResponse.diary_health,
                            workCapacity = "Высокая",
                            workCapacityScore = diaryResponse.diary_efficiency,
                            sleep = "Крепкий",
                            sleepScore = diaryResponse.diary_sleep,
                            appetite = "Хороший",
                            appetiteScore = diaryResponse.diary_appetite,
                            mood = "Бодрое",
                            moodScore = diaryResponse.diary_mood,
                            isSent = true
                        )
                    } ?: emptyList()

                    _uiState.value = _uiState.value.copy(diaryRecords = diaries)
                }
            } catch (e: Exception) {
                android.util.Log.e("DIARY_LOAD", "Ошибка: ${e.message}")
            }
        }
    }

    fun saveDiaryRecord(
        wellBeing: String, wellBeingScore: Int,
        workCapacity: String, workCapacityScore: Int,
        sleep: String, sleepScore: Int,
        appetite: String, appetiteScore: Int,
        mood: String, moodScore: Int
    ) {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch

            _uiState.value = _uiState.value.copy(sendState = SendState.Loading)

            try {
                val isoDate = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.getDefault()
                ).format(System.currentTimeMillis())

                val payload = DiaryPayload(
                    diary_patient = patientId,
                    diary_health = wellBeingScore,
                    diary_efficiency = workCapacityScore,
                    diary_sleep = sleepScore,
                    diary_appetite = appetiteScore,
                    diary_mood = moodScore,
                    diary_date = isoDate
                )

                val response = NetworkModule.api.createDiary(payload)

                if (response.isSuccessful) {
                    loadDiariesFromServer()
                    _uiState.value = _uiState.value.copy(sendState = SendState.Success)
                } else {
                    val error = response.errorBody()?.string() ?: "Ошибка сервера"
                    _uiState.value = _uiState.value.copy(sendState = SendState.Error(error))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sendState = SendState.Error("Сеть: ${e.message}")
                )
            }
        }
    }

    private fun loadDocumentsFromServer() {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch

            try {
                val response = NetworkModule.api.getDocuments(patientId)
                if (response.isSuccessful) {
                    val documents = response.body()?.map { docResponse ->
                        UploadedDocument(
                            id = docResponse.id,
                            patientId = patientId,
                            type = mapDocumentTypeFromServer(docResponse.document_type),
                            fileName = docResponse.document_name,
                            fileSize = 0,
                            uploadDate = docResponse.upload_date ?: "",
                            mimeType = "application/octet-stream",
                            isSent = true,
                            localFilePath = null
                        )
                    } ?: emptyList()

                    _uiState.value = _uiState.value.copy(uploadedDocuments = documents)
                }
            } catch (e: Exception) {
                android.util.Log.e("DOC_LOAD", "Ошибка: ${e.message}")
            }
        }
    }

    private fun mapDocumentTypeFromServer(serverType: String): String {
        return when (serverType.lowercase()) {
            "analyzes" -> "Анализы"
            "results" -> "Результаты обследования"
            "report" -> "Заключение врача"
            "other" -> "Прочее"
            else -> serverType
        }
    }

    fun addDocument(context: Context, type: String, name: String, uri: Uri) {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch

            _uiState.value = _uiState.value.copy(isUploadingFile = true)

            try {
                val localFile = copyFileToInternalStorage(context, uri, name.ifEmpty { "file" })
                if (localFile == null) {
                    _uiState.value = _uiState.value.copy(isUploadingFile = false)
                    return@launch
                }

                val fileBytes = localFile.readBytes()
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                val requestFile = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val multipartFile = MultipartBody.Part.createFormData(
                    "document_file",
                    localFile.name,
                    requestFile
                )

                val typeMapping = mapOf(
                    "Анализы" to "Analyzes",
                    "Результаты обследования" to "Results",
                    "Заключение врача" to "Report",
                    "Прочее" to "Other"
                )
                val djangoType = typeMapping[type] ?: "Other"

                val response = NetworkModule.api.uploadDocument(
                    patientId = patientId.toString().toRequestBody(),
                    docType = djangoType.toRequestBody(),
                    docName = name.ifEmpty { "file" }.toRequestBody(),
                    file = multipartFile
                )

                if (response.isSuccessful) {
                    loadDocumentsFromServer()
                }

                localFile.delete()
            } catch (e: Exception) {
                android.util.Log.e("DOC_UPLOAD", "💥 ${e.message}", e)
            } finally {
                _uiState.value = _uiState.value.copy(isUploadingFile = false)
            }
        }
    }

    private fun copyFileToInternalStorage(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val extension = fileName.substringAfterLast('.', "")
            val tempFileName = if (extension.isNotEmpty()) {
                "upload_${System.currentTimeMillis()}.$extension"
            } else {
                "upload_${System.currentTimeMillis()}"
            }
            val outputFile = File(context.cacheDir, tempFileName)

            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    private fun loadDoctorNotesFromServer() {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch

            try {
                val response = NetworkModule.api.getNotes(patientId)
                if (response.isSuccessful) {
                    val notesResponse = response.body() ?: emptyList()

                    val doctorIds = notesResponse.map { it.note_doctor }.distinct()

                    val doctorsMap = mutableMapOf<Int, String>()
                    for (doctorId in doctorIds) {
                        try {
                            val doctorResponse = NetworkModule.api.getDoctor(doctorId)
                            if (doctorResponse.isSuccessful) {
                                val doc = doctorResponse.body()
                                val name = if (doc != null) {
                                    val firstNameInitial = doc.doctor_first_name.firstOrNull()?.let { "$it." } ?: ""
                                    val thirdNameInitial = doc.doctor_third_name.firstOrNull()?.let { "$it." } ?: ""
                                    "${doc.doctor_second_name} $firstNameInitial$thirdNameInitial".trim()
                                } else {
                                    "Врач #$doctorId"
                                }
                                doctorsMap[doctorId] = name
                            } else {
                                doctorsMap[doctorId] = "Врач #$doctorId"
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("DOCTOR_LOAD", "Ошибка загрузки врача $doctorId: ${e.message}")
                            doctorsMap[doctorId] = "Врач #$doctorId"
                        }
                    }

                    val notes = notesResponse.map { note ->
                        DoctorNote(
                            id = note.id,
                            doctorName = doctorsMap[note.note_doctor] ?: "Неизвестный врач",
                            date = formatDate(note.note_date),
                            description = note.note_description ?: "Нет описания",
                            reason = note.note_reason
                        )
                    }

                    _uiState.value = _uiState.value.copy(doctorNotes = notes)
                } else {
                    android.util.Log.e("NOTES_LOAD", "Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NOTES_LOAD", "Ошибка сети: ${e.message}")
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString) ?: return dateString

            val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ru"))
            outputFormat.timeZone = java.util.TimeZone.getDefault()
            outputFormat.format(date)
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val date = inputFormat.parse(dateString) ?: return dateString
                val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ru"))
                outputFormat.format(date)
            } catch (e2: Exception) {
                dateString
            }
        }
    }

    private fun loadVitalsFromLocal() {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch
            _uiState.value = _uiState.value.copy(
                vitals = db.vitalDao().getRecent(patientId)
            )
        }
    }

    fun refreshMockData() {
        viewModelScope.launch {
            val patientId = getPatientId() ?: return@launch
            val mockVital = MockGenerator.generateVital().copy(patientId = patientId)
            db.vitalDao().insert(mockVital)
            loadVitalsFromLocal()
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            val patientId = getPatientId()
            if (patientId != null) {
                db.vitalDao().clearByPatient(patientId)
            }
        }
    }

    private fun parseDate(dateString: String): Long {
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd HH:mm:ss"
            )
            for (format in formats) {
                try {
                    return SimpleDateFormat(format, Locale.getDefault()).parse(dateString)?.time ?: 0
                } catch (e: Exception) {
                    continue
                }
            }
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}