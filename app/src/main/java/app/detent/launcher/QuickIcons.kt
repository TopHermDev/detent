package app.detent.launcher

/** The icons that the quick button can show in place of the app name. */
object QuickIcons {
    val names = listOf(
        "camera", "phone", "chat", "mail", "globe", "pin",
        "music", "headphones", "play", "photo", "calendar", "clock",
        "note", "book", "terminal", "code", "card", "bag",
        "gear", "sun", "heart", "star", "bolt", "home",
        "bell", "user", "game", "lock", "cloud",
    )

    // A word matches the start of a word in the app name or the package. A word with a space matches anywhere
    // in the app name. The first icon with a match wins, so the more exact words come first.
    private val words = listOf(
        "bag" to listOf("play store", "galaxy store", "store", "shop", "amazon", "market"),
        "lock" to listOf("password", "bitwarden", "authenticator", "vault", "keepass"),
        "music" to listOf("music", "spotify", "tidal", "deezer", "soundcloud"),
        "headphones" to listOf("podcast", "audible", "audiobook"),
        "camera" to listOf("camera"),
        "phone" to listOf("phone", "dialer"),
        "chat" to listOf("messag", "messenger", "sms", "signal", "whatsapp", "telegram", "chat", "discord", "slack"),
        "mail" to listOf("mail", "gmail", "outlook"),
        "globe" to listOf("chrome", "firefox", "browser", "internet", "brave", "opera", "vivaldi", "duckduckgo"),
        "pin" to listOf("maps", "waze", "navigation"),
        "play" to listOf("youtube", "video", "netflix", "player", "twitch", "plex"),
        "photo" to listOf("photo", "gallery"),
        "calendar" to listOf("calendar"),
        "clock" to listOf("clock", "alarm", "timer"),
        "note" to listOf("note", "keep", "obsidian", "memo"),
        "book" to listOf("book", "kindle", "reader"),
        "terminal" to listOf("termux", "terminal"),
        "code" to listOf("github", "gitlab", "code"),
        "card" to listOf("wallet", "pay", "bank", "vipps", "revolut"),
        "gear" to listOf("settings"),
        "sun" to listOf("weather"),
        "heart" to listOf("health", "fitness", "strava"),
        "bolt" to listOf("torch", "flashlight"),
        "home" to listOf("home"),
        "user" to listOf("contacts", "people"),
        "game" to listOf("game", "steam", "xbox"),
        "cloud" to listOf("drive", "cloud", "dropbox"),
    )

    /** The icon that fits an app with the name [label] and the package [pkg], or null when no icon fits. */
    fun suggest(label: String, pkg: String): String? {
        val l = label.lowercase()
        val names = l.split(Regex("[^\\p{L}\\p{N}]+"))
        val parts = pkg.lowercase().split('.', '_')
        fun hit(w: String, tokens: List<String>) = if (' ' in w) w in l else tokens.any { it.startsWith(w) }
        return words.firstOrNull { (_, w) -> w.any { hit(it, names) } }?.first
            ?: words.firstOrNull { (_, w) -> w.any { ' ' !in it && hit(it, parts) } }?.first
    }
}
