package com.example.medreminder.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.medreminder.R

object NotificationHelper {

    fun showAlarmNotification(context: Context, drugName: String, dosage: String) {
        val pi = PendingIntent.getActivity(
            context, 100,
            FullScreenAlarmActivity.intent(context, -1, drugName, dosage, ""),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, "med_alarm")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("该吃药啦")
            .setContentText("$drugName · $dosage")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(200, notification)
    }
}
