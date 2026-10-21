package app.shift.learn

import app.shift.learn.data.Lesson
import app.shift.learn.data.SourceFile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the bundled content the same way tools/validate_content.py does. Gradle runs unit tests from the app/ directory. */
class ContentAssetsTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val root = File("src/main/assets/content")
    private val sourceIds = json.decodeFromString<SourceFile>(File(root, "sources.json").readText()).sources.map { it.id }.toSet()
    private val lessons = File(root, "lessons").listFiles { f -> f.name.endsWith(".json") }!!.map { json.decodeFromString<Lesson>(it.readText()) }

    @Test fun thereIsAtLeastOneLesson() = assertTrue(lessons.isNotEmpty())
    @Test fun strictSafetyLessonsHaveStopAndRefer() =
        lessons.filter { it.safety.level == "strict" }.forEach { l -> assertTrue("${l.id} lacks stop-and-refer", l.blocks.any { it.type == "stop-and-refer" }) }
    @Test fun everyCitedSourceExistsAndIsListed() = lessons.forEach { l ->
        val cited = l.blocks.flatMap { b -> b.figures.orEmpty().map { it.sourceId } } + l.questions.flatMap { it.sourceIds } + l.misconceptions.flatMap { it.sourceIds }
        val listed = l.references.map { it.sourceId }.toSet()
        cited.forEach { assertTrue("${l.id}: $it not in registry", it in sourceIds); assertTrue("${l.id}: $it not in references", it in listed) }
    }
    @Test fun answersPointAtRealOptions() = lessons.forEach { l ->
        l.questions.filter { it.type == "multiple-choice" || it.type == "multi-select" }.forEach { q ->
            assertTrue("${l.id}/${q.id}", q.correctOptionIds.isNotEmpty() && q.options.map { it.id }.containsAll(q.correctOptionIds))
        }
    }
    @Test fun regionSpecificLessonsNameRegions() =
        lessons.filter { it.jurisdiction.scope == "region-specific" }.forEach { assertTrue(it.id, it.jurisdiction.regions.isNotEmpty()) }
}
