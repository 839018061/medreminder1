package com.example.medreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.medreminder.data.AppDatabase
import com.example.medreminder.data.repository.MedicationRepository

class MedReminderApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        MedicationRepository(database.drugDao(), database.scheduleDao(), database.recordDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "med_alarm",
                getString(R.string.channel_alarm_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.channel_alarm_desc) }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
