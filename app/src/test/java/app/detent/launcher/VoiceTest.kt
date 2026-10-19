package app.detent.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceTest {
    private val apps = listOf("calendar", "camera", "clock", "maps", "whatsapp", "youtube", "youtube music", "yt music")

    private fun find(said: String) = Voice.match(Voice.name(said), apps) { it }

    @Test fun stripsCommandWords() {
        assertEquals("camera", Voice.name("Open the Camera"))
        assertEquals("spotify", Voice.name("launch spotify app"))
        assertEquals("kamera", Voice.name("åpne kamera"))
        assertEquals("clock", Voice.name("  clock "))
    }

    @Test fun exactNameWins() {
        assertEquals("camera", find("camera"))
        assertEquals("youtube", find("YouTube"))
        assertEquals("youtube music", find("open youtube music"))
    }

    @Test fun ignoresSpacesAndCase() = assertEquals("whatsapp", find("Whats App"))

    @Test fun singlePrefixMatches() = assertEquals("clock", find("clo"))

    @Test fun twoPrefixesDoNotMatch() = assertNull(find("ca"))

    @Test fun nameInsideThePhraseMatches() = assertEquals("maps", find("google maps"))

    @Test fun unknownNameDoesNotMatch() {
        assertNull(find("weather"))
        assertNull(find("open"))
    }
}
