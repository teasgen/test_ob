package com.example.healthmonitor.data.mock
import com.example.healthmonitor.data.VitalRecord
import kotlin.random.Random
object MockGenerator {
    fun generateVital(): VitalRecord = VitalRecord(
        timestamp = System.currentTimeMillis(),
        steps = Random.nextInt(300, 1200),
        heartRate = Random.nextInt(60, 105),
        spO2 = Random.nextInt(95, 99),
        temperature = Random.nextFloat() * 0.5f + 36.4f
    )
}