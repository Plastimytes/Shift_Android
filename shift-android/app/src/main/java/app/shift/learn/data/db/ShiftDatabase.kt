package app.shift.learn.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lesson_progress")
data class LessonProgress(@PrimaryKey val lessonId: String, val completed: Boolean, val updatedAt: Long)

@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: String,
    val lessonVersion: String,
    val correct: Int,
    val total: Int,
    val passed: Boolean,
    val takenAt: Long,
)

@Entity(tableName = "bookmarks")
data class Bookmark(@PrimaryKey val lessonId: String, val createdAt: Long)

@Dao
interface ShiftDao {
    @Query("SELECT lessonId FROM lesson_progress WHERE completed = 1")
    fun completedIds(): Flow<List<String>>

    @Query("SELECT lessonId FROM bookmarks")
    fun bookmarkIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(p: LessonProgress)

    @Insert
    suspend fun insertAttempt(a: QuizAttempt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(b: Bookmark)

    @Query("DELETE FROM bookmarks WHERE lessonId = :lessonId")
    suspend fun removeBookmark(lessonId: String)
}

// Version 1. Any schema change needs a Migration plus a test; exported schemas live in app/schemas.
@Database(entities = [LessonProgress::class, QuizAttempt::class, Bookmark::class], version = 1, exportSchema = true)
abstract class ShiftDatabase : RoomDatabase() {
    abstract fun dao(): ShiftDao

    companion object {
        fun build(context: Context): ShiftDatabase =
            Room.databaseBuilder(context, ShiftDatabase::class.java, "shift.db").build()
    }
}
