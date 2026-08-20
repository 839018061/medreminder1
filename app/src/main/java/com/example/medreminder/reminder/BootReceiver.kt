package com.example.medreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medreminder.MedReminderApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as MedReminderApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleAll(app)
            } finally {
                pending.finish()
            }
        }
    }
}

suspend fun rescheduleAll(app: MedReminderApp) {
    val schedules = app.repository.allSchedules()
    val drugs = app.repository.allDrugs()
    schedules.filter { it.active }.forEach { s ->
        val d = drugs.find { it.id == s.drugId }
        if (d != null) AlarmScheduler.schedule(app, s, d.name, d.dosage)
    }
}
