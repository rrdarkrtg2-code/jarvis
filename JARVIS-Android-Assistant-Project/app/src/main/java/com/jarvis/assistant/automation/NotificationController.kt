package com.jarvis.assistant.automation

import android.app.Notification
import android.service.notification.StatusBarNotification

data class NotificationSummary(
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

object NotificationController {

    private var isConnected = false
    private val currentNotifications = mutableListOf<StatusBarNotification>()

    fun setConnected(connected: Boolean) {
        isConnected = connected
    }

    fun refreshActiveNotifications(notifications: List<StatusBarNotification>) {
        synchronized(currentNotifications) {
            currentNotifications.clear()
            currentNotifications.addAll(notifications)
        }
    }

    fun isNotificationAccessGranted(): Boolean = isConnected

    fun getNotificationCount(): Int {
        synchronized(currentNotifications) {
            return currentNotifications.size
        }
    }

    fun summarizeNotifications(): String {
        if (!isConnected) {
            return "Notification access is not enabled. Please enable Notification Access in Settings to allow J.A.R.V.I.S. to read notifications."
        }

        val notifications = synchronized(currentNotifications) {
            currentNotifications.mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                if (title.isNotEmpty() || text.isNotEmpty()) {
                    NotificationSummary(sbn.packageName, title, text, sbn.postTime)
                } else null
            }
        }

        if (notifications.isEmpty()) {
            return "You have no active notifications."
        }

        val appCount = notifications.groupBy { it.packageName }.map { (pkg, list) ->
            val simpleName = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            "$simpleName: ${list.size}"
        }.joinToString(", ")

        return "You have ${notifications.size} unread notifications across: $appCount."
    }
}
