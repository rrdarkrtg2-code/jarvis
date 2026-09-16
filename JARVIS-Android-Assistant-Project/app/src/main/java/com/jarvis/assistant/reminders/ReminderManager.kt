package com.jarvis.assistant.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.data.repository.ReminderRepository

class ReminderManager(
    private val context: Context,
    private val reminderRepository: ReminderRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    suspend fun scheduleReminder(title: String, delayMinutes: Int): String {
        val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        val id = reminderRepository.addReminder(title, triggerTime)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = Constants.ACTION_REMINDER_ALERT
            putExtra(Constants.EXTRA_REMINDER_ID, id)
            putExtra(Constants.EXTRA_REMINDER_TITLE, title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            return "Reminder set for $delayMinutes minute${if (delayMinutes > 1) "s" else ""} from now: '$title'."
        } catch (e: SecurityException) {
            return "Unable to schedule exact alarm. Please grant Exact Alarm permission in Android Settings."
        } catch (e: Exception) {
            return "Failed to set reminder: ${e.message}"
        }
    }
}
