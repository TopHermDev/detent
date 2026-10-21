package app.detent.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickTest {
    private val two = listOf(QuickSlot("camera", "camera"), QuickSlot("phone", null))

    private fun keys(l: List<QuickSlot>) = l.map { it.key }

    @Test fun aSlotPastTheEndAddsAButton() {
        assertEquals(listOf("camera", "phone", "maps"), keys(two.put(2, "maps", "pin")))
        assertEquals(listOf("camera"), keys(emptyList<QuickSlot>().put(0, "camera", null)))
    }

    @Test fun aSlotInTheListChangesThatButton() {
        val out = two.put(1, "maps", "pin")
        assertEquals(listOf(QuickSlot("camera", "camera"), QuickSlot("maps", "pin")), out)
    }

    @Test fun theSameAppGetsANewIcon() = assertEquals(QuickSlot("phone", "chat"), two.put(1, "phone", "chat")[1])

    @Test fun anAppMovesToTheSlotThatItTakes() {
        // The app leaves its old button. The app that it replaces leaves the corner.
        val out = listOf(QuickSlot("a", null), QuickSlot("b", null), QuickSlot("c", null)).put(2, "a", "star")
        assertEquals(listOf(QuickSlot("b", null), QuickSlot("a", "star")), out)
    }

    @Test fun addingAnAppThatHasAButtonChangesThatButton() {
        val out = two.put(2, "camera", "photo")
        assertEquals(listOf(QuickSlot("camera", "photo"), QuickSlot("phone", null)), out)
    }

    @Test fun theCornerHoldsThreeButtons() {
        val three = two + QuickSlot("maps", null)
        assertEquals(3, three.put(3, "mail", null).size)
    }
}
