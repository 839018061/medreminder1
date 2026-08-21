package com.example.medreminder.domain

import com.example.medreminder.data.entity.AdherenceRecord

object AdherenceCalculator {

    /** 计分规则：按时=1，补服=0.5，跳过=0.5，漏服=0，待处理/小睡中不计分 */
    fun score(status: String): Double = when (status) {
        AdherenceRecord.TAKEN -> 1.0
        AdherenceRecord.LATE -> 0.5
        AdherenceRecord.SKIPPED -> 0.5
        else -> 0.0
    }

    /** 是否已结束（计入分母）：TAKEN / LATE / SKIPPED / MISSED */
    fun isFinalized(status: String): Boolean = when (status) {
        AdherenceRecord.TAKEN, AdherenceRecord.LATE,
        AdherenceRecord.SKIPPED, AdherenceRecord.MISSED -> true
        else -> false
    }

    fun rate(records: List<AdherenceRecord>): Float {
        val finalized = records.filter { isFinalized(it.status) }
        if (finalized.isEmpty()) return 1f
        val total = finalized.sumOf { score(it.status) }
        return (total / finalized.size).toFloat()
    }

    data class Summary(
        val total: Int,
        val taken: Int,
        val late: Int,
        val skipped: Int,
        val missed: Int,
        val rate: Float
    )

    fun summarize(records: List<AdherenceRecord>): Summary {
        val taken = records.count { it.status == AdherenceRecord.TAKEN }
        val late = records.count { it.status == AdherenceRecord.LATE }
        val skipped = records.count { it.status == AdherenceRecord.SKIPPED }
        val missed = records.count { it.status == AdherenceRecord.MISSED }
        return Summary(records.size, taken, late, skipped, missed, rate(records))
    }
}
