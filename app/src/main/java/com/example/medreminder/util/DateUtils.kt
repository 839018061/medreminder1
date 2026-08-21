package com.example.medreminder.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun today(): String = dateFmt.format(System.currentTimeMillis())
    fun dateOf(millis: Long): String = dateFmt.format(millis)

    fun daysAgo(n: Int): String {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -n)
        return dateFmt.format(c.time)
    }

    fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 0) to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    /** 计算下一次符合 repeatDays 的触发时间戳（毫秒） */
    fun nextTriggerMillis(time: String, repeatDays: String): Long {
        val (h, m) = parseTime(time)
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        repeat(8) {
            if (cal.timeInMillis > now.timeInMillis && matchesDay(cal, repeatDays)) {
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    /** 今天 00:00 的时间戳（毫秒） */
    fun startOfDayMillis(date: String = today()): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun todayAtMillis(time: String): Long {
        val (h, m) = parseTime(time)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun isScheduledToday(repeatDays: String): Boolean {
        if (repeatDays == "daily") return true
        return matchesDay(Calendar.getInstance(), repeatDays)
    }

    private fun matchesDay(cal: Calendar, repeatDays: String): Boolean {
        if (repeatDays == "daily") return true
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val iso = if (dow == Calendar.SUNDAY) 7 else dow - 1
        return repeatDays.split(",").any { it.trim().toIntOrNull() == iso }
    }
}
