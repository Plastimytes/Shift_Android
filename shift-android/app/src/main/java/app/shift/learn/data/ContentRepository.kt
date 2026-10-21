package app.shift.learn.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Reads bundled JSON from assets/content. Offline by construction. */
class ContentRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun readText(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    suspend fun load(): ContentState = withContext(Dispatchers.IO) {
        val categories = json.decodeFromString<CategoryFile>(readText("content/categories.json"))
        val curriculum = json.decodeFromString<Curriculum>(readText("content/curriculum.json"))
        val sources = json.decodeFromString<SourceFile>(readText("content/sources.json")).sources.associateBy { it.id }
        val lessons = context.assets.list("content/lessons").orEmpty()
            .filter { it.endsWith(".json") }
            .map { json.decodeFromString<Lesson>(readText("content/lessons/$it")) }
            .sortedBy { it.id }
        ContentState(categories, curriculum, lessons, sources)
    }
}
