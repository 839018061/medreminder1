package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 服药打卡记录 */
@Entity(tableName = "adherence_record")
data class AdherenceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val drugName: String,     // 冗余药品名，便于统计展示
    val planDate: String,     // 计划日期 "2026-08-17"
    val planTime: Long,       // 计划时间戳（毫秒）
    val actualTime: Long?,    // 实际打卡时间戳，null 表示尚未处理
    val status: String        // TAKEN(按时) / LATE(补服) / MISSED(漏服)
) {
    companion object {
        const val TAKEN = "TAKEN"
        const val LATE = "LATE"
        const val MISSED = "MISSED"
    }
}
