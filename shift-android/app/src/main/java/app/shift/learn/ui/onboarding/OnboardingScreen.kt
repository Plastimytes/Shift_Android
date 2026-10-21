package app.shift.learn.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import app.shift.learn.data.CategoryItem
import app.shift.learn.data.ContentState
import app.shift.learn.domain.InterestSelection
import app.shift.learn.ui.common.BigTitle

@Composable
fun OnboardingScreen(
    content: ContentState,
    selection: InterestSelection,
    onToggle: (String) -> Unit,
    onDone: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<String?>(null) }
    val groupNames = remember(content) { content.categories.groups.associate { it.id to it.name } }
    val visible = remember(query, group, content) {
        content.categories.items.filter { (group == null || it.groupId == group) && it.name.contains(query.trim(), ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SHIFT", fontWeight = FontWeight.ExtraBold, letterSpacing = androidx.compose.ui.unit.TextUnit(3f, androidx.compose.ui.unit.TextUnitType.Sp))
        BigTitle("Pick your\nthree.")
        Text("Everything stays open. These choices shape what you see first, and you can change them later.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            label = { Text("Search interests") }, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = group == null, onClick = { group = null }, label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
            }
            items(content.categories.groups, key = { it.id }) { g ->
                FilterChip(selected = group == g.id, onClick = { group = g.id }, label = { Text(g.short) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
            }
        }
        Text("${selection.selected.size} of ${InterestSelection.REQUIRED} interests selected", fontWeight = FontWeight.ExtraBold)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visible, key = { it.id }) { item ->
                CategoryRow(item, groupNames[item.groupId].orEmpty(), item.id in selection.selected,
                    canAdd = !selection.isComplete) { onToggle(item.id) }
            }
        }
        Button(onClick = onDone, enabled = selection.isComplete, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(if (selection.isComplete) "Start learning" else "Choose ${selection.remaining} more to continue", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun CategoryRow(item: CategoryItem, groupName: String, selected: Boolean, canAdd: Boolean, onToggle: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.ExtraBold)
                Text(groupName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.size(8.dp))
            FilledIconButton(
                onClick = onToggle, enabled = selected || canAdd, modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface),
            ) {
                Icon(if (selected) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = if (selected) "Remove ${item.name}" else "Select ${item.name}")
            }
        }
    }
}
