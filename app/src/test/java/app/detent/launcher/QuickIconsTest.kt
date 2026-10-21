package app.detent.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickIconsTest {
    @Test fun nameFindsTheIcon() {
        assertEquals("camera", QuickIcons.suggest("camera", "com.sec.android.app.camera"))
        assertEquals("chat", QuickIcons.suggest("whatsapp", "com.whatsapp"))
        assertEquals("mail", QuickIcons.suggest("gmail", "com.google.android.gm"))
        assertEquals("gear", QuickIcons.suggest("settings", "com.android.settings"))
    }

    @Test fun moreExactWordsWin() {
        assertEquals("bag", QuickIcons.suggest("play store", "com.android.vending"))
        assertEquals("music", QuickIcons.suggest("youtube music", "com.google.android.apps.youtube.music"))
        assertEquals("lock", QuickIcons.suggest("keepassdx", "com.kunzisoft.keepass.free"))
    }

    @Test fun wordsMatchOnlyAtTheStart() {
        // "pay" must not match inside "display".
        assertNull(QuickIcons.suggest("display", "com.example.display"))
    }

    @Test fun packageFindsTheIconWhenTheNameDoesNot() {
        assertEquals("clock", QuickIcons.suggest("uhr", "com.example.clock"))
        assertEquals("phone", QuickIcons.suggest("telefon", "com.samsung.android.dialer"))
    }

    @Test fun everySuggestionIsAnIcon() {
        val labels = listOf("camera", "spotify", "maps", "chrome", "photos", "calendar", "termux", "weather", "steam", "drive")
        for (l in labels) assertTrue(l, QuickIcons.suggest(l, "") in QuickIcons.names)
    }
}
