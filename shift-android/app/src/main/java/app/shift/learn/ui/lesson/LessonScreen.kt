package app.shift.learn.ui.lesson

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.shift.learn.data.Block
import app.shift.learn.data.Lesson
import app.shift.learn.data.Source
import app.shift.learn.ui.common.InfoChip
import app.shift.learn.ui.common.ModePills

@Composable
fun LessonScreen(
    lesson: Lesson,
    mode: String,
    sources: Map<String, Source>,
    bookmarked: Boolean,
    completed: Boolean,
    onMode: (String) -> Unit,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
    onQuiz: () -> Unit,
) {
    // If the learner's mode has no tailored version, fall back to the lesson's first mode and say so.
    val hasMode = mode in lesson.modes
    val effective = if (hasMode) mode else lesson.modes.first()
    var showSources by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            IconButton(onClick = onBookmark, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Star, contentDescription = if (bookmarked) "Remove bookmark" else "Bookmark lesson",
                    tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            }
        }
        Text(lesson.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            InfoChip("${lesson.estimatedMinutes} min")
            InfoChip(lesson.difficulty.replaceFirstChar { it.uppercase() })
            if (lesson.safety.level != "none") InfoChip("Safety", inverted = true)
            if (lesson.review.state == "draft") InfoChip("Draft")
        }
        ModePills(mode, onMode)
        if (!hasMode) Text("No version for this mode yet. Showing the ${effective} version.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(lesson.introduction, style = MaterialTheme.typography.bodyLarge)
        Text("You will learn", fontWeight = FontWeight.ExtraBold)
        lesson.objectives.forEach { Text("• $it") }

        lesson.blocks.filter { it.modes == null || effective in it.modes }.forEach { BlockView(it) }

        if (lesson.keyTerms.isNotEmpty()) {
            Text("Key terms", fontWeight = FontWeight.ExtraBold)
            lesson.keyTerms.forEach { Text("${it.term}: ${it.definition}") }
        }
        if (lesson.misconceptions.isNotEmpty()) {
            Text("Common misconceptions", fontWeight = FontWeight.ExtraBold)
            lesson.misconceptions.forEach { Text("Myth: ${it.belief}\nReality: ${it.correction}") }
        }
        Text("Summary", fontWeight = FontWeight.ExtraBold)
        Text(lesson.summary)
        if (lesson.jurisdiction.scope == "region-specific")
            Text("Applies to: ${lesson.jurisdiction.regions.joinToString()}. Other regions have different rules.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (showSources) {
            lesson.references.forEach { r ->
                val s = sources[r.sourceId]
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${r.sourceId} · ${s?.title ?: "Unknown source"}", fontWeight = FontWeight.Bold)
                        if (s != null) Text("${s.issuer} · Tier ${s.tier} · confidence ${s.confidence}", style = MaterialTheme.typography.bodySmall)
                        Text("${r.locator}: ${r.supports}", style = MaterialTheme.typography.bodySmall)
                        if (s != null) Text(s.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (lesson.review.openIssues.isNotEmpty()) {
                Text("Open review issues", fontWeight = FontWeight.ExtraBold)
                lesson.review.openIssues.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { showSources = !showSources }, modifier = Modifier.height(56.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {
                Text(if (showSources) "Hide sources" else "Sources", fontWeight = FontWeight.ExtraBold)
            }
            Button(onClick = onQuiz, modifier = Modifier.weight(1f).height(56.dp)) {
                Text(if (completed) "Retake quiz" else "Test your knowledge", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun BlockView(b: Block) {
    when (b.type) {
        "text" -> Text(b.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)
        "specification-table" -> SpecTable(b)
        "safety-note" -> Callout("SAFETY", b.text.orEmpty(), inverted = true)
        "stop-and-refer" -> Callout("STOP AND REFER", b.text.orEmpty(), inverted = false)
        // diagram and interactive blocks are defined in the schema but not built yet; say so instead of hiding them.
        else -> Text("[${b.type} content is not included in this build]", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun fmt(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

@Composable
private fun SpecTable(b: Block) {
    Surface(shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(b.caption.orEmpty(), fontWeight = FontWeight.ExtraBold)
            b.figures.orEmpty().forEach { f ->
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(f.label)
                        Text("${fmt(f.value)} ${f.unit}", fontWeight = FontWeight.Bold)
                    }
                    Text("Source ${f.sourceId}, ${f.locator}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Callout(title: String, body: String, inverted: Boolean) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (inverted) cs.inverseSurface else cs.surface,
        contentColor = if (inverted) cs.inverseOnSurface else cs.onSurface,
        border = if (inverted) null else BorderStroke(2.dp, cs.onSurface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            Text(body)
        }
    }
}
