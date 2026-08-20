package com.example.medreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val drugId = intent.getLongExtra("drugId", -1L)
        val drugName = intent.getStringExtra("drugName") ?: "用药"
        val time = intent.getStringExtra("time") ?: ""
        val timing = intent.getStringExtra("timing") ?: ""
        NotificationHelper.showAlarm(context, drugId, drugName, time, timing)
    }
}
