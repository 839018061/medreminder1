package com.example.medreminder

import android.app.Application
import com.example.medreminder.reminder.NotificationHelper

class MedReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
