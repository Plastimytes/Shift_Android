package app.shift.learn.domain

import app.shift.learn.data.Question
import app.shift.learn.data.Safety
import kotlin.math.abs

sealed interface Answer {
    data class Options(val ids: Set<String>) : Answer
    data class Bool(val value: Boolean) : Answer
    data class Numeric(val text: String) : Answer
}

data class QuizResult(
    val correct: Int,
    val total: Int,
    val threshold: Double,
    val passed: Boolean,
    val correctIds: Set<String>,
) {
    val fraction: Double get() = if (total == 0) 0.0 else correct.toDouble() / total
}

object QuizScorer {
    const val DEFAULT_THRESHOLD = 0.8

    /** Strict-safety lessons use their own mastery threshold (default 100%); everything else 80%. */
    fun requiredThreshold(safety: Safety): Double =
        if (safety.level == "strict") (safety.masteryThreshold ?: 1.0).coerceIn(DEFAULT_THRESHOLD, 1.0)
        else DEFAULT_THRESHOLD

    fun isCorrect(q: Question, a: Answer?): Boolean = when (q.type) {
        "multiple-choice", "multi-select" -> a is Answer.Options && a.ids == q.correctOptionIds.toSet()
        "true-false" -> a is Answer.Bool && q.correctBoolean != null && a.value == q.correctBoolean
        "calculation" -> {
            val target = q.correctNumber
            val typed = (a as? Answer.Numeric)?.text?.trim()?.replace(',', '.')?.toDoubleOrNull()
            target != null && typed != null && abs(typed - target.value) <= target.tolerance
        }
        else -> false // unsupported question types never count as correct
    }

    fun score(questions: List<Question>, answers: Map<String, Answer>, threshold: Double): QuizResult {
        val ok = questions.filter { isCorrect(it, answers[it.id]) }.map { it.id }.toSet()
        val fraction = if (questions.isEmpty()) 0.0 else ok.size.toDouble() / questions.size
        return QuizResult(ok.size, questions.size, threshold, questions.isNotEmpty() && fraction >= threshold - 1e-9, ok)
    }
}
