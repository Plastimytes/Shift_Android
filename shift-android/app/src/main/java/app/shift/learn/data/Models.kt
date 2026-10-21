package app.shift.learn.data

import kotlinx.serialization.Serializable

// Mirrors content-src/shift_lesson.schema.json. Unknown fields are ignored so newer content stays readable.

@Serializable
data class Lesson(
    val schemaVersion: String,
    val id: String,
    val version: String,
    val title: String,
    val section: String,
    val moduleId: String,
    val modes: List<String>,
    val categories: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val estimatedMinutes: Int,
    val difficulty: String,
    val objectives: List<String>,
    val introduction: String,
    val blocks: List<Block>,
    val keyTerms: List<KeyTerm> = emptyList(),
    val misconceptions: List<Misconception> = emptyList(),
    val summary: String,
    val questions: List<Question>,
    val references: List<Reference>,
    val safety: Safety,
    val jurisdiction: Jurisdiction,
    val vehicleSpecific: VehicleSpecific? = null,
    val review: Review,
    val revisedOn: String,
    val nextLessonId: String? = null,
)

@Serializable
data class Block(
    val type: String,
    val text: String? = null,
    val modes: List<String>? = null,
    val caption: String? = null,
    val figures: List<Figure>? = null,
    val level: String? = null,
    val registerId: String? = null,
    val assetId: String? = null,
    val altText: String? = null,
    val toolId: String? = null,
)

@Serializable
data class Figure(val label: String, val value: Double, val unit: String, val sourceId: String, val locator: String)

@Serializable
data class KeyTerm(val term: String, val definition: String)

@Serializable
data class Misconception(val belief: String, val correction: String, val sourceIds: List<String>)

@Serializable
data class Option(val id: String, val text: String)

@Serializable
data class CorrectNumber(val value: Double, val tolerance: Double, val unit: String)

@Serializable
data class Question(
    val id: String,
    val type: String,
    val prompt: String,
    val options: List<Option> = emptyList(),
    val correctOptionIds: List<String> = emptyList(),
    val correctBoolean: Boolean? = null,
    val correctNumber: CorrectNumber? = null,
    val explanation: String,
    val difficulty: String,
    val sourceIds: List<String>,
)

@Serializable
data class Reference(val sourceId: String, val locator: String, val supports: String)

@Serializable
data class Safety(val level: String, val registerIds: List<String> = emptyList(), val masteryThreshold: Double? = null)

@Serializable
data class Jurisdiction(val scope: String, val regions: List<String> = emptyList())

@Serializable
data class VehicleSpecific(val isVehicleSpecific: Boolean, val specificationSourceId: String? = null)

@Serializable
data class Review(
    val state: String,
    val reviewer: String? = null,
    val reviewedOn: String? = null,
    val openIssues: List<String> = emptyList(),
)

// ---- bundled catalogue files ----
@Serializable data class CategoryGroup(val id: String, val name: String, val short: String)
@Serializable data class CategoryItem(val id: String, val name: String, val groupId: String)
@Serializable data class CategoryFile(val groups: List<CategoryGroup>, val items: List<CategoryItem>)

@Serializable data class Module(val id: String, val section: String, val title: String, val tier: String, val plannedLessons: Int)
@Serializable data class Curriculum(val modules: List<Module>)

@Serializable data class Source(
    val id: String, val title: String, val issuer: String, val url: String,
    val tier: String, val reuse: String, val confidence: String,
)
@Serializable data class SourceFile(val sources: List<Source>)

/** Everything read from bundled assets. Nothing here ever touches the network. */
data class ContentState(
    val categories: CategoryFile,
    val curriculum: Curriculum,
    val lessons: List<Lesson>,
    val sources: Map<String, Source>,
)
