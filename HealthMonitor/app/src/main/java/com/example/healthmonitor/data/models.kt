package com.example.healthmonitor.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "vitals")
data class VitalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int = 0,
    val timestamp: Long,
    val steps: Int,
    val heartRate: Int,
    val spO2: Int,
    val temperature: Float,
    val sleepTime: String = "",
    val sleepScore: Int = 0,
    val stressLevel: Int = 0
)

@Entity(tableName = "diary_local")
data class DiaryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int = 0,
    val timestamp: Long,
    val wellBeing: String,
    val wellBeingScore: Int,
    val workCapacity: String,
    val workCapacityScore: Int,
    val sleep: String,
    val sleepScore: Int,
    val appetite: String,
    val appetiteScore: Int,
    val mood: String,
    val moodScore: Int,
    val isSent: Boolean = false
)

@Entity(tableName = "uploaded_documents")
data class UploadedDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int = 0,
    val type: String = "",
    val fileName: String,
    val fileSize: Long,
    val uploadDate: String,
    val mimeType: String,
    val isSent: Boolean = false,
    val localFilePath: String? = null
)

data class HealthPayload(
    @SerializedName("user_id") val userId: String = "patient_01",
    @SerializedName("vitals") val vitals: List<VitalRecord>,
    @SerializedName("files_count") val filesCount: Int = 0
)