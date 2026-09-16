package com.jarvis.assistant.automation

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class JarvisNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationController.setConnected(true)
        NotificationController.refreshActiveNotifications(activeNotifications?.toList() ?: emptyList())
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationController.setConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        NotificationController.refreshActiveNotifications(activeNotifications?.toList() ?: emptyList())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        NotificationController.refreshActiveNotifications(activeNotifications?.toList() ?: emptyList())
    }
}
