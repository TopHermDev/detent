package app.detent.launcher

import kotlin.math.max

/** A hint for the app with [key]: add it to the dial, or remove it. [count] gives the opens or the idle days. */
class Suggestion(val key: String, val add: Boolean, val count: Int)

/** The rules for the dial hints. All times are milliseconds. */
object Suggest {
    const val DAY = 86_400_000L
    const val WEEK = 7 * DAY

    /** An app off the dial that opens this many times from search in a week earns a hint to add it. */
    const val OPENS = 3

    /** A dial app that does not open from the home screen for this long earns a hint to remove it. */
    const val IDLE = 21 * DAY

    /** The dial keeps at least this many apps. */
    const val MIN_DIAL = 3

    /** The shortest time between two hints. */
    const val GAP = 20 * 3_600_000L

    /** After "not now", the hint to add the app stays away this long. */
    const val NOT_NOW = 30 * DAY

    /** After "keep", the hint to remove the app stays away this long. */
    const val KEEP = 90 * DAY

    /** A hint that gets no answer comes back after this long. */
    const val LATER = 3 * DAY

    /**
     * The hint to show now, or null. A hint to add an app comes before a hint to remove one.
     *
     * @param dial keys of the apps on the dial.
     * @param installed keys of all installed apps.
     * @param searches times of the opens from search, by key.
     * @param lastOpen the last open from the home screen, by key.
     * @param since the time that each dial app joined the dial, by key.
     * @param quiet the time until which each app gets no hint, by key.
     */
    fun pick(
        now: Long,
        dial: Set<String>,
        installed: Set<String>,
        searches: Map<String, List<Long>>,
        lastOpen: Map<String, Long>,
        since: Map<String, Long>,
        quiet: Map<String, Long>,
    ): Suggestion? {
        fun free(key: String) = (quiet[key] ?: 0L) <= now
        val add = searches.entries
            .filter { (key, _) -> key !in dial && key in installed && free(key) }
            .map { (key, times) -> key to times.count { now - it in 0 until WEEK } }
            .filter { it.second >= OPENS }
            .maxByOrNull { it.second }
        if (add != null) return Suggestion(add.first, true, add.second)
        if (dial.size <= MIN_DIAL) return null
        // An app counts as used from the time it joined the dial.
        val idle = dial.filter(::free)
            .map { it to max(lastOpen[it] ?: 0L, since[it] ?: now) }
            .filter { now - it.second >= IDLE }
            .minByOrNull { it.second } ?: return null
        return Suggestion(idle.first, false, ((now - idle.second) / DAY).toInt())
    }
}
