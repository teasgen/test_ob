package com.example.healthmonitor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthmonitor.data.DiaryRecord
import com.example.healthmonitor.data.UploadedDocument
import com.example.healthmonitor.data.VitalRecord

@Dao
interface VitalDao {
    @Query("SELECT * FROM vitals WHERE patientId = :patientId ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecent(patientId: Int): List<VitalRecord>

    @Insert
    suspend fun insert(vital: VitalRecord)

    @Query("DELETE FROM vitals WHERE patientId = :patientId")
    suspend fun clearByPatient(patientId: Int)

    @Query("DELETE FROM vitals")
    suspend fun clear()
}

@Dao
interface DiaryRecordDao {
    @Query("SELECT * FROM diary_local WHERE patientId = :patientId ORDER BY timestamp DESC")
    suspend fun getAll(patientId: Int): List<DiaryRecord>

    @Insert
    suspend fun insert(record: DiaryRecord)

    @Query("DELETE FROM diary_local WHERE patientId = :patientId")
    suspend fun clearByPatient(patientId: Int)

    @Query("DELETE FROM diary_local")
    suspend fun clear()

    @Query("UPDATE diary_local SET isSent = :sent WHERE id = :recordId AND patientId = :patientId")
    suspend fun markAsSent(recordId: Int, sent: Boolean, patientId: Int)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM uploaded_documents WHERE patientId = :patientId ORDER BY uploadDate DESC")
    suspend fun getAll(patientId: Int): List<UploadedDocument>

    @Insert
    suspend fun insert(doc: UploadedDocument): Long

    @Query("DELETE FROM uploaded_documents WHERE patientId = :patientId")
    suspend fun clearByPatient(patientId: Int)

    @Query("UPDATE uploaded_documents SET isSent = :sent WHERE id = :docId AND patientId = :patientId")
    suspend fun markAsSent(docId: Int, sent: Boolean, patientId: Int)
}