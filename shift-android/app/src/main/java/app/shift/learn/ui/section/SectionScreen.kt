package app.shift.learn.ui.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.shift.learn.data.ContentState
import app.shift.learn.ui.common.BigTitle
import app.shift.learn.ui.common.InfoChip

/** Shows every planned module for a section and says plainly which ones have lessons in this build. */
@Composable
fun SectionScreen(title: String, section: String, content: ContentState, completed: Set<String>, onOpen: (String) -> Unit) {
    val modules = content.curriculum.modules.filter { it.section == section }
    LazyColumn(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BigTitle(title) }
        item {
            Text("Modules marked Planned are on the roadmap and are not in this build yet.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(modules, key = { it.id }) { m ->
            val lessons = content.lessons.filter { it.moduleId == m.id }
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(m.title, Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
                        InfoChip(if (lessons.isEmpty()) "Planned" else "${lessons.size} available", inverted = lessons.isNotEmpty())
                    }
                    lessons.forEach { l ->
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().clickable { onOpen(l.id) }) {
                            Text(l.title + if (l.id in completed) "  (mastered)" else "", Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
