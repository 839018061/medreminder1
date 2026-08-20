package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_schedules")
data class DoseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugId: Long,
    val time: String,          // HH:mm
    val timing: String = "早餐后",  // 早餐前/早餐后/午餐后/晚餐后/睡前
    val repeatDays: List<Int> = listOf(1,2,3,4,5,6,7), // 1=周一 ... 7=周日
    val enabled: Boolean = true
)
