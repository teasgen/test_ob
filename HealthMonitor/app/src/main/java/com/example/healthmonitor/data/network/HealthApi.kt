package com.example.healthmonitor.data.network

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

interface HealthApi {
    @POST("api/patients/login/")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/patients/register/")
    suspend fun registerPatient(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("api/diaries/")
    suspend fun getDiaries(@Query("diary_patient") patientId: Int): Response<List<DiaryResponse>>

    @POST("api/diaries/")
    suspend fun createDiary(@Body payload: DiaryPayload): Response<DiaryResponse>

    @GET("api/documents/")
    suspend fun getDocuments(@Query("document_patient") patientId: Int): Response<List<DocumentResponse>>

    @Multipart
    @POST("api/documents/")
    suspend fun uploadDocument(
        @Part("document_patient") patientId: RequestBody,
        @Part("document_type") docType: RequestBody,
        @Part("document_name") docName: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<JsonObject>

    @GET("api/notes/")
    suspend fun getNotes(@Query("note_patient") patientId: Int): Response<List<NoteResponse>>

    @GET("api/doctors/{id}/")
    suspend fun getDoctor(@Path("id") id: Int): Response<DoctorResponse>
}

data class AuthRequest(
    val patient_email: String? = null,
    val patient_phone_number: String? = null,
    val patient_password: String
)

data class RegisterRequest(
    val patient_email: String,
    val patient_phone_number: String,
    val patient_password: String
)

data class AuthResponse(
    val status: String,
    val patient_id: Int,
    val token: String,
    val invite_code: String? = null,
)


data class DiaryResponse(
    val id: Int,
    val diary_health: Int,
    val diary_efficiency: Int,
    val diary_sleep: Int,
    val diary_appetite: Int,
    val diary_mood: Int,
    val diary_date: String
)

data class DiaryPayload(
    val diary_patient: Int,
    val diary_health: Int,
    val diary_efficiency: Int,
    val diary_sleep: Int,
    val diary_appetite: Int,
    val diary_mood: Int,
    val diary_date: String
)

data class DocumentResponse(
    val id: Int,
    val document_patient: Int,
    val document_type: String,
    val document_name: String,
    val document_file: String,
    val upload_date: String? = null
)

data class NoteResponse(
    val id: Int,
    val note_patient: Int,
    val note_doctor: Int,
    val note_description: String?,
    val note_reason: String,
    val note_date: String
)

data class DoctorResponse(
    val id: Int,
    val doctor_first_name: String = "",
    val doctor_second_name: String = "",
    val doctor_third_name: String = ""
) {
    fun getFullName(): String {
        val firstNameInitial = doctor_first_name.firstOrNull()?.let { "$it." } ?: ""
        val thirdNameInitial = doctor_third_name.firstOrNull()?.let { "$it." } ?: ""
        return "${doctor_second_name} $firstNameInitial$thirdNameInitial".trim()
    }
}

fun String.toRequestBody(mediaType: String = "text/plain"): RequestBody {
    return RequestBody.create(mediaType.toMediaTypeOrNull(), this)
}