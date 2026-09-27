package com.example.healthmonitor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthmonitor.data.DiaryRecord
import com.example.healthmonitor.data.VitalRecord
import com.example.healthmonitor.data.UploadedDocument

@Database(entities = [VitalRecord::class, DiaryRecord::class, UploadedDocument::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vitalDao(): VitalDao
    abstract fun diaryRecordDao(): DiaryRecordDao
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "health_db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}