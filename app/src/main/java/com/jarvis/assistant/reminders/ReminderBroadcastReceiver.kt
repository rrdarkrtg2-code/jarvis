package com.jarvis.assistant.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.MainActivity
import com.jarvis.assistant.R
import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.data.local.JarvisDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(Constants.EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(Constants.EXTRA_REMINDER_TITLE) ?: "Reminder from J.A.R.V.I.S."

        showNotification(context, reminderId, title)

        if (reminderId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                JarvisDatabase.getInstance(context).reminderDao().markCompleted(reminderId)
            }
        }
    }

    private fun showNotification(context: Context, reminderId: Long, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_REMINDERS,
                "J.A.R.V.I.S. Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for scheduled tasks and reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_jarvis_logo)
            .setContentTitle("J.A.R.V.I.S. Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((Constants.REMINDER_NOTIFICATION_ID_BASE + reminderId).toInt(), notification)
    }
}
