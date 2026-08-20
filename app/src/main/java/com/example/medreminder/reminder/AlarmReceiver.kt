package com.example.medreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.medreminder.MedReminderApp
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_DOSE_ALARM) return
        val scheduleId = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
        val drugName = intent.getStringExtra(AlarmScheduler.EXTRA_DRUG_NAME) ?: ""
        val dosage = intent.getStringExtra(AlarmScheduler.EXTRA_DOSAGE) ?: ""
        val relation = intent.getStringExtra(AlarmScheduler.EXTRA_RELATION) ?: ""

        val pending = goAsync()
        val app = context.applicationContext as MedReminderApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = app.repository
                val existing = repo.recordFor(scheduleId, DateUtils.today())
                if (existing == null) {
                    repo.insertOrUpdateRecord(
                        AdherenceRecord(
                            scheduleId = scheduleId,
                            drugName = drugName,
                            planDate = DateUtils.today(),
                            planTime = System.currentTimeMillis(),
                            actualTime = null,
                            status = AdherenceRecord.MISSED
                        )
                    )
                }
            } finally {
                pending.finish()
            }
        }

        ContextCompat.startForegroundService(context, ReminderService.intent(context))
        FullScreenAlarmActivity.start(context, scheduleId, drugName, dosage, relation)
        NotificationHelper.showAlarmNotification(context, drugName, dosage)
    }
}
