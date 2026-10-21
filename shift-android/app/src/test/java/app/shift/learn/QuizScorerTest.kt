package app.shift.learn

import app.shift.learn.data.CorrectNumber
import app.shift.learn.data.Option
import app.shift.learn.data.Question
import app.shift.learn.data.Safety
import app.shift.learn.domain.Answer
import app.shift.learn.domain.QuizScorer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScorerTest {
    private fun mc(id: String) = Question(id, "multiple-choice", "p", listOf(Option("a", "A"), Option("b", "B")), listOf("a"), null, null, "e", "easy", listOf("S01"))
    private fun tf(id: String) = Question(id, "true-false", "p", correctBoolean = false, explanation = "e", difficulty = "easy", sourceIds = listOf("S01"))
    private fun calc(id: String) = Question(id, "calculation", "p", correctNumber = CorrectNumber(311.0, 1.0, "F"), explanation = "e", difficulty = "medium", sourceIds = listOf("S01"))
    private fun ms(id: String) = Question(id, "multi-select", "p", listOf(Option("a", "A"), Option("b", "B"), Option("c", "C")), listOf("a", "c"), null, null, "e", "easy", listOf("S01"))

    @Test fun multipleChoice() {
        assertTrue(QuizScorer.isCorrect(mc("q"), Answer.Options(setOf("a"))))
        assertFalse(QuizScorer.isCorrect(mc("q"), Answer.Options(setOf("b"))))
        assertFalse(QuizScorer.isCorrect(mc("q"), null))
    }
    @Test fun multiSelectNeedsExactSet() {
        assertTrue(QuizScorer.isCorrect(ms("q"), Answer.Options(setOf("c", "a"))))
        assertFalse(QuizScorer.isCorrect(ms("q"), Answer.Options(setOf("a"))))
        assertFalse(QuizScorer.isCorrect(ms("q"), Answer.Options(setOf("a", "b", "c"))))
    }
    @Test fun trueFalse() {
        assertTrue(QuizScorer.isCorrect(tf("q"), Answer.Bool(false)))
        assertFalse(QuizScorer.isCorrect(tf("q"), Answer.Bool(true)))
    }
    @Test fun calculationHonoursToleranceAndCommaDecimals() {
        assertTrue(QuizScorer.isCorrect(calc("q"), Answer.Numeric("311")))
        assertTrue(QuizScorer.isCorrect(calc("q"), Answer.Numeric(" 310,5 ")))
        assertFalse(QuizScorer.isCorrect(calc("q"), Answer.Numeric("320")))
        assertFalse(QuizScorer.isCorrect(calc("q"), Answer.Numeric("abc")))
    }
    @Test fun thresholds() {
        assertEquals(0.8, QuizScorer.requiredThreshold(Safety("none")), 1e-9)
        assertEquals(1.0, QuizScorer.requiredThreshold(Safety("strict")), 1e-9)
        assertEquals(0.8, QuizScorer.requiredThreshold(Safety("strict", masteryThreshold = 0.1)), 1e-9) // never below 80%
    }
    @Test fun defaultPassMarkIs80Percent() {
        val qs = (1..5).map { mc("q$it") }
        val four = qs.associate { it.id to Answer.Options(setOf("a")) }.toMutableMap().also { it["q5"] = Answer.Options(setOf("b")) }
        assertTrue(QuizScorer.score(qs, four, 0.8).passed)
        val three = four.toMutableMap().also { it["q4"] = Answer.Options(setOf("b")) }
        assertFalse(QuizScorer.score(qs, three, 0.8).passed)
    }
    @Test fun strictLessonNeedsEveryAnswerRight() {
        val qs = listOf(mc("q1"), tf("q2"), calc("q3"))
        val miss = mapOf("q1" to Answer.Options(setOf("a")), "q2" to Answer.Bool(false), "q3" to Answer.Numeric("0"))
        assertFalse(QuizScorer.score(qs, miss, 1.0).passed)
        val all = mapOf("q1" to Answer.Options(setOf("a")), "q2" to Answer.Bool(false), "q3" to Answer.Numeric("311"))
        assertTrue(QuizScorer.score(qs, all, 1.0).passed)
    }
    @Test fun emptyQuizNeverPasses() = assertFalse(QuizScorer.score(emptyList(), emptyMap(), 0.8).passed)
}
