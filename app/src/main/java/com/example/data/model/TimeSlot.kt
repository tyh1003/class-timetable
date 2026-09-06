package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_slots")
data class TimeSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val semesterId: Long = 0, // 0 = global/default
    val code: String,
    val startTime: String,
    val endTime: String,
    val orderIndex: Int = 0
) {
    val displayTime: String
        get() = "$startTime ~ $endTime"
}
