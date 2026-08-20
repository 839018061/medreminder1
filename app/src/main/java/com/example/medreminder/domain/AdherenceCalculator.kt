package com.example.medreminder.domain

import com.example.medreminder.data.entity.AdherenceRecord

object AdherenceCalculator {

    fun score(status: String): Double = when (status) {
        AdherenceRecord.TAKEN -> 1.0
        AdherenceRecord.LATE -> 0.5
        else -> 0.0
    }

    fun rate(records: List<AdherenceRecord>): Float {
        if (records.isEmpty()) return 1f
        val total = records.sumOf { score(it.status) }
        return (total / records.size).toFloat()
    }

    data class Summary(
        val total: Int,
        val taken: Int,
        val late: Int,
        val missed: Int,
        val rate: Float
    )

    fun summarize(records: List<AdherenceRecord>): Summary {
        val taken = records.count { it.status == AdherenceRecord.TAKEN }
        val late = records.count { it.status == AdherenceRecord.LATE }
        val missed = records.count { it.status == AdherenceRecord.MISSED }
        return Summary(records.size, taken, late, missed, rate(records))
    }
}
