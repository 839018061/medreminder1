package com.example.medreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medreminder.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val schedules = db.scheduleDao().getAll().let { runBlockingOrNull(it) }
                // Reschedule all after reboot
                schedules?.forEach { AlarmScheduler.schedule(context, it) }
            }
        }
    }

    private fun runBlockingOrNull(f: kotlinx.coroutines.flow.Flow<List<com.example.medreminder.data.entity.DoseSchedule>>): List<com.example.medreminder.data.entity.DoseSchedule>? {
        return try {
            kotlinx.coroutines.runBlocking { f.first() }
        } catch (e: Exception) { null }
    }
}
