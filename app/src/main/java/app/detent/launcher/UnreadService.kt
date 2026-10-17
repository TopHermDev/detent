package app.detent.launcher

import android.app.Notification
import android.os.UserManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow

/** Latest post time of a clearable notification, keyed by package plus user serial. */
object Unread {
    val state = MutableStateFlow<Map<String, Long>>(emptyMap())
}

/** Feeds the unread dots. It runs only after the user grants notification access. */
class UnreadService : NotificationListenerService() {
    override fun onListenerConnected() = refresh()

    override fun onListenerDisconnected() {
        Unread.state.value = emptyMap()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val list = try {
            activeNotifications
        } catch (e: RuntimeException) {
            null
        } ?: return
        val um = getSystemService(UserManager::class.java)
        val map = HashMap<String, Long>()
        for (sbn in list) {
            val flags = sbn.notification.flags
            if (sbn.isOngoing) continue
            if (flags and (Notification.FLAG_FOREGROUND_SERVICE or Notification.FLAG_GROUP_SUMMARY) != 0) continue
            val key = sbn.packageName + "#" + um.getSerialNumberForUser(sbn.user)
            map[key] = maxOf(map[key] ?: 0L, sbn.postTime)
        }
        Unread.state.value = map
    }
}
