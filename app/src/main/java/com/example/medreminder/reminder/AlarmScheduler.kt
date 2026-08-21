package com.example.medreminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.util.DateUtils

object AlarmScheduler {

    const val ACTION_DOSE_ALARM = "com.example.medreminder.ACTION_DOSE_ALARM"
    const val EXTRA_SCHEDULE_ID = "schedule_id"
    const val EXTRA_DRUG_NAME = "drug_name"
    const val EXTRA_DOSAGE = "dosage"
    const val EXTRA_RELATION = "relation"

    const val MAX_SNOOZE = 3
    private const val SNOOZE_MS = 10 * 60 * 1000L // 小睡 10 分钟

    fun schedule(context: Context, schedule: DoseSchedule, drugName: String, dosage: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, schedule, drugName, dosage)
        val triggerAt = DateUtils.nextTriggerMillis(schedule.time, schedule.repeatDays)
        setExact(am, triggerAt, pi)
    }

    /** 小睡重排：10 分钟后再次提醒，超过最大次数则不再重排（由调用方标记 MISSED） */
    fun snooze(context: Context, schedule: DoseSchedule, drugName: String, dosage: String, snoozeCount: Int): Boolean {
        if (snoozeCount >= MAX_SNOOZE) return false
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, schedule, drugName, dosage)
        val triggerAt = System.currentTimeMillis() + SNOOZE_MS
        setExact(am, triggerAt, pi)
        return true
    }

    private fun setExact(am: AlarmManager, triggerAt: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            return
        }
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancel(context: Context, scheduleId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DOSE_ALARM
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        }
        val pi = PendingIntent.getBroadcast(
            context, scheduleId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        pi.cancel()
    }

    private fun pendingIntent(
        context: Context,
        schedule: DoseSchedule,
        drugName: String,
        dosage: String
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DOSE_ALARM
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_DRUG_NAME, drugName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_RELATION, schedule.relation)
        }
        return PendingIntent.getBroadcast(
            context, schedule.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
