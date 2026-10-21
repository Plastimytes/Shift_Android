package app.shift.learn.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.shift.learn.data.ContentState
import app.shift.learn.data.UserPrefs
import app.shift.learn.ui.common.BigTitle
import app.shift.learn.ui.common.InfoChip
import app.shift.learn.ui.common.ModePills

@Composable
fun HomeScreen(
    prefs: UserPrefs,
    content: ContentState,
    completed: Set<String>,
    bookmarks: Set<String>,
    onMode: (String) -> Unit,
    onOpen: (String) -> Unit,
    onBookmark: (String) -> Unit,
) {
    val names = content.categories.items.associate { it.id to it.name }
    val next = content.lessons.firstOrNull { it.id !in completed }
    val planned = content.curriculum.modules.sumOf { it.plannedLessons }
    LazyColumn(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { BigTitle("Learn the\nmachine.") }
        item { ModePills(prefs.mode, onMode) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                prefs.interests.forEach { InfoChip(names[it] ?: it) }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.fillMaxWidth()
                    .let { m -> if (next != null) m.clickable { onOpen(next.id) } else m }) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (next != null) "CONTINUE" else "ALL DONE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(next?.title ?: "You have finished every lesson in this build.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        item {
            Text("${content.lessons.size} lesson(s) in this build, out of roughly $planned planned (planning estimate).",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(content.lessons, key = { it.id }) { l ->
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().clickable { onOpen(l.id) }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(l.title, fontWeight = FontWeight.ExtraBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            InfoChip("${l.estimatedMinutes} min")
                            InfoChip(l.difficulty.replaceFirstChar { it.uppercase() })
                            if (l.safety.level != "none") InfoChip("Safety", inverted = true)
                            if (l.id in completed) InfoChip("Mastered")
                        }
                    }
                    IconButton(onClick = { onBookmark(l.id) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = if (l.id in bookmarks) "Remove bookmark" else "Bookmark lesson",
                            tint = if (l.id in bookmarks) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
