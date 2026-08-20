package com.example.medreminder.domain

import com.example.medreminder.data.entity.AdherenceRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AdherenceCalculator {
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /** 计算某药在指定日期是否已按时服药 */
    fun hasTaken(records: List<AdherenceRecord>, drugId: Long, date: String): Boolean {
        return records.any { it.drugId == drugId && it.date == date && it.taken }
    }

    /** 最近 N 天服药率 */
    fun adherenceRate(records: List<AdherenceRecord>, drugId: Long, days: Int = 30): Float {
        if (days <= 0) return 0f
        var taken = 0
        var total = 0
        val cal = Calendar.getInstance()
        repeat(days) { i ->
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val date = fmt.format(cal.time)
            total++
            if (hasTaken(records, drugId, date)) taken++
            cal.add(Calendar.DAY_OF_YEAR, i)
        }
        return if (total == 0) 0f else taken.toFloat() / total
    }
}
