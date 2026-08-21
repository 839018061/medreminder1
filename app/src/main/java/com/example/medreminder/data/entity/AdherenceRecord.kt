package com.example.medreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 服药打卡记录（提醒状态机）
 *
 * 状态流转：
 *  pending  -> taken(按时) / late(补服) / skipped(跳过) / missed(漏服)
 *  pending  -> snoozed(已小睡) -> taken / late / skipped / missed
 */
@Entity(tableName = "adherence_record")
data class AdherenceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val drugName: String,     // 冗余药品名，便于统计展示
    val planDate: String,     // 计划日期 "2026-08-17"
    val planTime: Long,       // 计划时间戳（毫秒）
    val actualTime: Long?,    // 实际打卡时间戳，null 表示尚未处理
    val status: String,       // PENDING(待处理) / TAKEN(按时) / LATE(补服) / SKIPPED(跳过) / SNOOZED(小睡中) / MISSED(漏服)
    val snoozeCount: Int = 0, // 已小睡次数（最多 3 次）
    val takenAt: Long? = null // 实际完成打卡的时间戳（用于 5 分钟内撤销）
) {
    companion object {
        const val PENDING = "PENDING"
        const val TAKEN = "TAKEN"
        const val LATE = "LATE"
        const val SKIPPED = "SKIPPED"
        const val SNOOZED = "SNOOZED"
        const val MISSED = "MISSED"
    }
}
