package app.shift.learn

import app.shift.learn.domain.InterestSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterestSelectionTest {
    @Test fun startsEmptyAndIncomplete() {
        val s = InterestSelection()
        assertFalse(s.isComplete); assertEquals(3, s.remaining)
    }
    @Test fun completesAtExactlyThree() {
        val s = InterestSelection().toggle("a").toggle("b")
        assertFalse(s.isComplete); assertEquals(1, s.remaining)
        assertTrue(s.toggle("c").isComplete)
    }
    @Test fun fourthSelectionIsIgnored() {
        val s = InterestSelection().toggle("a").toggle("b").toggle("c").toggle("d")
        assertEquals(setOf("a", "b", "c"), s.selected)
    }
    @Test fun removingReopensTheLastSlot() {
        val s = InterestSelection().toggle("a").toggle("b").toggle("c").toggle("b")
        assertFalse(s.isComplete); assertEquals(setOf("a", "c"), s.selected)
        assertTrue(s.toggle("d").isComplete)
    }
}
