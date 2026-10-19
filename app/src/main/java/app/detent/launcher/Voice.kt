package app.detent.launcher

/** Finds the app that a voice command names. */
object Voice {
    /** Words that can come before an app name in a voice command. */
    private val verbs = listOf("open", "launch", "start", "run", "show", "go to", "åpne")

    /** The app name in a phrase such as "open the camera". */
    fun name(said: String): String {
        var t = said.trim().lowercase()
        verbs.firstOrNull { t.startsWith("$it ") }?.let { t = t.removePrefix("$it ") }
        return t.removePrefix("the ").removeSuffix(" app").trim()
    }

    /** The item that [name] names, or null when no single item is a sure match. */
    fun <T> match(name: String, items: List<T>, label: (T) -> String): T? {
        val n = squash(name)
        if (n.isEmpty()) return null
        val all = items.map { it to squash(label(it)) }
        all.firstOrNull { it.second == n }?.let { return it.first }
        all.filter { it.second.startsWith(n) }.singleOrNull()?.let { return it.first }
        // "google maps" names the app "maps". The longest name inside the phrase wins.
        val inside = all.filter { it.second.length >= 3 && it.second in n }
        val best = inside.maxOfOrNull { it.second.length } ?: return null
        return inside.filter { it.second.length == best }.singleOrNull()?.first
    }

    /** Lower case, letters and digits only, so "WhatsApp" and "whats app" are the same. */
    private fun squash(s: String) = s.lowercase().filter { it.isLetterOrDigit() }
}
