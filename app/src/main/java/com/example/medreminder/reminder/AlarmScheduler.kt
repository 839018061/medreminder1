package com.example.medreminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medreminder.data.entity.DoseSchedule
import java.util.Calendar

object AlarmScheduler {
    fun schedule(context: Context, schedule: DoseSchedule) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val parts = schedule.time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val pi = pendingIntent(context, schedule)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun cancel(context: Context, schedule: DoseSchedule) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, schedule))
    }

    private fun pendingIntent(context: Context, s: DoseSchedule): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("drugId", s.drugId)
            putExtra("scheduleId", s.id)
        }
        return PendingIntent.getBroadcast(
            context, s.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
