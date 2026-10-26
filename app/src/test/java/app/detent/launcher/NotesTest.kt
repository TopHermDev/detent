package app.detent.launcher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesTest {
    @Test fun aPlainNotificationShows() {
        assertTrue(shadeShows(false, false, false, "title", "text"))
    }

    @Test fun aTitleAloneIsEnough() {
        assertTrue(shadeShows(false, false, false, "title", ""))
    }

    @Test fun anOngoingNotificationStaysOut() {
        assertFalse(shadeShows(true, false, false, "title", "text"))
    }

    @Test fun aForegroundServiceNotificationStaysOut() {
        assertFalse(shadeShows(false, true, false, "title", "text"))
    }

    @Test fun aGroupSummaryStaysOut() {
        assertFalse(shadeShows(false, false, true, "title", "text"))
    }

    @Test fun aNotificationWithNoWordsStaysOut() {
        assertFalse(shadeShows(false, false, false, "", ""))
    }
}
