package app.detent.launcher

/** One notification, as the shade shows it. */
class Note(
    /** The key that Android gives the notification. */
    val key: String,
    val pkg: String,
    /** The label of the app that posted it. */
    val app: String,
    val title: String,
    val text: String,
    /** The time that it arrived. */
    val time: Long,
)

/**
 * True when the shade shows a notification. Ongoing notifications and group summaries stay out, the same
 * ones that the unread dots skip, and a notification with no title and no text has nothing to show.
 */
fun shadeShows(ongoing: Boolean, foregroundService: Boolean, groupSummary: Boolean, title: String, text: String) =
    !ongoing && !foregroundService && !groupSummary && (title.isNotEmpty() || text.isNotEmpty())
