package com.example.medreminder.reminder

import android.Manifest
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    private const val CHANNEL_ID = "medication_reminders"

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "服药提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "到点提醒服药"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun showAlarm(context: Context, drugId: Long, drugName: String, time: String, timing: String) {
        ensureChannel(context)
        val intent = Intent(context, FullScreenAlarmActivity::class.java).apply {
            putExtra("drugName", drugName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(context, drugId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("服药时间到")
            .setContentText("$timing 服用：$drugName")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
        try {
            NotificationManagerCompat.from(context).notify(drugId.toInt(), builder.build())
        } catch (_: SecurityException) {}
    }
}
