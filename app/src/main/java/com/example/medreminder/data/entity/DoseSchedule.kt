package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 服药计划（某药品在某个时间点的一次剂量） */
@Entity(tableName = "dose_schedule")
data class DoseSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drugId: Long,
    val time: String,        // "08:00"
    val repeatDays: String,  // "daily" 表示每天；否则为星期集合，如 "1,3,5"（周一、三、五）
    val relation: String = "无", // 饭前 / 饭后 / 睡前 / 无
    val active: Boolean = true
)
