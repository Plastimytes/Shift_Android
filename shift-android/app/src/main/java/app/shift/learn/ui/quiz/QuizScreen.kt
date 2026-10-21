package app.shift.learn.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.shift.learn.data.Lesson
import app.shift.learn.data.Option
import app.shift.learn.data.Question
import app.shift.learn.domain.Answer
import app.shift.learn.domain.QuizResult
import app.shift.learn.domain.QuizScorer

@Composable
fun QuizScreen(lesson: Lesson, onBack: () -> Unit, onRecord: (QuizResult) -> Unit) {
    var attempt by rememberSaveable { mutableIntStateOf(0) }
    // Question and answer order is shuffled for every attempt.
    val questions = remember(attempt) { lesson.questions.shuffled().map { it.copy(options = it.options.shuffled()) } }
    val answers = remember(attempt) { mutableStateMapOf<String, Answer>() }
    var result by remember(attempt) { mutableStateOf<QuizResult?>(null) }
    val threshold = QuizScorer.requiredThreshold(lesson.safety)

    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to lesson") }
        Text("Test your knowledge", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(lesson.title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Pass mark: ${(threshold * 100).toInt()}%" + if (lesson.safety.level == "strict") " (strict, because this lesson covers a safety-critical system)" else "",
            style = MaterialTheme.typography.bodySmall)

        questions.forEachIndexed { i, q ->
            QuestionCard(i + 1, q, answers[q.id], result != null, result?.correctIds?.contains(q.id)) { answers[q.id] = it }
        }

        val r = result
        if (r == null) {
            Button(onClick = {
                val scored = QuizScorer.score(questions, answers.toMap(), threshold)
                result = scored
                onRecord(scored)
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Submit answers", fontWeight = FontWeight.ExtraBold) }
        } else {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${r.correct} of ${r.total} correct (${(r.fraction * 100).toInt()}%)", fontWeight = FontWeight.ExtraBold)
                    if (r.passed) Text("Passed. This lesson is marked mastered in this app. That is learning progress, not a professional qualification.")
                    else Text("Not yet. Re-read the lesson sections for the questions you missed, then retake.")
                }
            }
            if (!r.passed) Button(onClick = { attempt++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Retake quiz", fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun QuestionCard(n: Int, q: Question, answer: Answer?, locked: Boolean, correct: Boolean?, onAnswer: (Answer) -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("$n. ${q.prompt}", fontWeight = FontWeight.ExtraBold)
            when (q.type) {
                "multiple-choice" -> q.options.forEach { o -> ChoiceRow(o, (answer as? Answer.Options)?.ids?.contains(o.id) == true, locked, radio = true) { onAnswer(Answer.Options(setOf(o.id))) } }
                "multi-select" -> q.options.forEach { o ->
                    val cur = (answer as? Answer.Options)?.ids.orEmpty()
                    ChoiceRow(o, o.id in cur, locked, radio = false) { onAnswer(Answer.Options(if (o.id in cur) cur - o.id else cur + o.id)) }
                }
                "true-false" -> listOf(true to "True", false to "False").forEach { (v, label) ->
                    ChoiceRow(Option(label, label), (answer as? Answer.Bool)?.value == v, locked, radio = true) { onAnswer(Answer.Bool(v)) }
                }
                "calculation" -> OutlinedTextField(
                    value = (answer as? Answer.Numeric)?.text.orEmpty(), onValueChange = { onAnswer(Answer.Numeric(it)) },
                    enabled = !locked, singleLine = true, label = { Text("Answer in ${q.correctNumber?.unit.orEmpty()}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                else -> Text("[${q.type} questions are not supported in this build]")
            }
            if (correct != null) {
                Text(if (correct) "Correct" else "Not quite", fontWeight = FontWeight.ExtraBold)
                Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
                Text("Sources: ${q.sourceIds.joinToString()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ChoiceRow(o: Option, selected: Boolean, locked: Boolean, radio: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected = selected, enabled = !locked, role = if (radio) Role.RadioButton else Role.Checkbox, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically) {
        if (radio) RadioButton(selected = selected, onClick = null, enabled = !locked) else Checkbox(checked = selected, onCheckedChange = null, enabled = !locked)
        Text(o.text, Modifier.padding(start = 8.dp))
    }
}
