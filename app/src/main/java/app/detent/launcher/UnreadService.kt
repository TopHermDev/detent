package app.detent.launcher

import android.app.Notification
import android.os.UserManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow

/** Latest post time of a clearable notification, keyed by package plus user serial. */
object Unread {
    val state = MutableStateFlow<Map<String, Long>>(emptyMap())

    /** The notifications the shade shows, newest first. */
    val notes = MutableStateFlow<List<Note>>(emptyList())
}

/** Feeds the unread dots and the shade. It runs only after the user grants notification access. */
class UnreadService : NotificationListenerService() {
    /** The label of each app that has posted a notification. */
    private val labels = HashMap<String, String>()

    override fun onListenerConnected() = refresh()

    override fun onListenerDisconnected() {
        Unread.state.value = emptyMap()
        Unread.notes.value = emptyList()
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
        val notes = ArrayList<Note>()
        for (sbn in list) {
            // One notification that the phone refuses must not hide the rest.
            runCatching {
                val n = sbn.notification
                val flags = n.flags
                val ongoing = sbn.isOngoing
                val service = flags and Notification.FLAG_FOREGROUND_SERVICE != 0
                val summary = flags and Notification.FLAG_GROUP_SUMMARY != 0
                if (ongoing || service || summary) return@runCatching
                val key = sbn.packageName + "#" + um.getSerialNumberForUser(sbn.user)
                map[key] = maxOf(map[key] ?: 0L, sbn.postTime)
                val title = n.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
                val text = (n.extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
                    ?: n.extras?.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
                if (shadeShows(ongoing, service, summary, title, text)) {
                    notes += Note(sbn.key, sbn.packageName, label(sbn.packageName), title, text, sbn.postTime)
                }
            }
        }
        Unread.state.value = map
        Unread.notes.value = notes.sortedByDescending { it.time }
    }

    /** The label of the app, kept for the life of the listener. */
    private fun label(pkg: String) = labels.getOrPut(pkg) {
        runCatching { packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString() }
            .getOrDefault(pkg)
    }
}
