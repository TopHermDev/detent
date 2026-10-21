package app.detent.launcher

import app.detent.launcher.Suggest.DAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestTest {
    private val now = 1_000 * DAY
    private val dial = setOf("camera", "clock", "maps", "phone")
    private val installed = dial + setOf("spotify", "steam")

    // Every dial app joined the dial 60 days ago and opened yesterday.
    private val since = dial.associateWith { now - 60 * DAY }
    private val used = dial.associateWith { now - DAY }

    private fun pick(
        searches: Map<String, List<Long>> = emptyMap(),
        lastOpen: Map<String, Long> = used,
        since: Map<String, Long> = this.since,
        quiet: Map<String, Long> = emptyMap(),
        dial: Set<String> = this.dial,
    ) = Suggest.pick(now, dial, installed, searches, lastOpen, since, quiet)

    private fun days(vararg d: Int) = d.map { now - it * DAY }

    @Test fun nothingToSay() = assertNull(pick())

    @Test fun threeSearchesThisWeekAddTheApp() {
        val s = pick(searches = mapOf("spotify" to days(1, 3, 6)))!!
        assertEquals("spotify", s.key)
        assertTrue(s.add)
        assertEquals(3, s.count)
    }

    @Test fun oldSearchesDoNotCount() = assertNull(pick(searches = mapOf("spotify" to days(1, 3, 9))))

    @Test fun theMostSearchedAppWins() {
        val s = pick(searches = mapOf("spotify" to days(1, 2, 3), "steam" to days(1, 2, 3, 4)))!!
        assertEquals("steam", s.key)
    }

    @Test fun appsOnTheDialAndUninstalledAppsGetNoAddHint() {
        assertNull(pick(searches = mapOf("maps" to days(1, 2, 3))))
        assertNull(pick(searches = mapOf("gone" to days(1, 2, 3))))
    }

    @Test fun aQuietAppGetsNoHint() {
        assertNull(pick(searches = mapOf("spotify" to days(1, 2, 3)), quiet = mapOf("spotify" to now + DAY)))
        assertNull(pick(lastOpen = used + ("maps" to now - 30 * DAY), quiet = mapOf("maps" to now + DAY)))
    }

    @Test fun anIdleDialAppGetsARemoveHint() {
        val s = pick(lastOpen = used + ("maps" to now - 30 * DAY))!!
        assertEquals("maps", s.key)
        assertTrue(!s.add)
        assertEquals(30, s.count)
    }

    @Test fun theLongestIdleAppWins() {
        val s = pick(lastOpen = used + ("maps" to now - 30 * DAY) + ("clock" to now - 40 * DAY))!!
        assertEquals("clock", s.key)
    }

    @Test fun aNewDialAppIsNotIdle() {
        assertNull(pick(lastOpen = used - "maps", since = since + ("maps" to now - 5 * DAY)))
        // Without a time on the dial, the app counts as new.
        assertNull(pick(lastOpen = used - "maps", since = since - "maps"))
    }

    @Test fun theDialKeepsThreeApps() {
        val three = setOf("camera", "clock", "maps")
        assertNull(pick(dial = three, lastOpen = emptyMap()))
    }

    @Test fun addComesBeforeRemove() {
        val s = pick(searches = mapOf("spotify" to days(1, 2, 3)), lastOpen = used + ("maps" to now - 30 * DAY))!!
        assertEquals("spotify", s.key)
    }
}
