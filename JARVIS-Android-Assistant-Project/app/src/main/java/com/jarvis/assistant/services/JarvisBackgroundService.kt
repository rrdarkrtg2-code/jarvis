package com.jarvis.assistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jarvis.assistant.MainActivity
import com.jarvis.assistant.R
import com.jarvis.assistant.core.Constants

class JarvisBackgroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServiceNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SERVICE,
                "J.A.R.V.I.S. Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains J.A.R.V.I.S. assistant active status"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE)
            .setContentTitle("J.A.R.V.I.S. Active")
            .setContentText("System online and ready for voice commands")
            .setSmallIcon(R.drawable.ic_jarvis_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(Constants.SERVICE_NOTIFICATION_ID, notification)
    }
}
