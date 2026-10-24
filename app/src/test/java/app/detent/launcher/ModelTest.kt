package app.detent.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

/** The wallpaper palette is an ordered scale, so the theme takes its ends by luminance. */
class ModelTest {
    private val black = 0xFF000000L
    private val white = 0xFFFFFFFFL
    private val grey = 0xFF808080L

    @Test fun aDarkSchemeTakesTheDarkerTone() {
        assertEquals(black, endTone(black, white, true))
        assertEquals(black, endTone(white, black, true))
    }

    @Test fun aLightSchemeTakesTheLighterTone() {
        assertEquals(white, endTone(black, white, false))
        assertEquals(white, endTone(white, black, false))
    }

    @Test fun theAccentStandsOutFromTheBackground() {
        assertEquals(white, standTone(black, white, black))
        assertEquals(black, standTone(black, white, white))
        assertEquals(black, standTone(grey, black, white))
    }
}
