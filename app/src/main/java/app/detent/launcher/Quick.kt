package app.detent.launcher

/** An app on a quick button. [icon] shows in place of the name. Null shows the name. */
data class QuickSlot(val key: String, val icon: String?)

/** The bottom corner holds this many quick buttons at most. */
const val QUICK_MAX = 3

/**
 * Puts the app [key] with [icon] on quick button [slot]. A slot past the end adds a button. Each app has
 * one button at most: an app that has a button already moves to [slot], or keeps its button when [slot]
 * adds a new one.
 */
fun List<QuickSlot>.put(slot: Int, key: String, icon: String?): List<QuickSlot> {
    val old = indexOfFirst { it.key == key }
    if (slot >= size && old >= 0) return put(old, key, icon)
    val out = toMutableList()
    var at = slot.coerceIn(0, size)
    if (old >= 0 && old != at) {
        out.removeAt(old)
        if (old < at) at--
    }
    if (at < out.size) out[at] = QuickSlot(key, icon) else out.add(QuickSlot(key, icon))
    return out.take(QUICK_MAX)
}
