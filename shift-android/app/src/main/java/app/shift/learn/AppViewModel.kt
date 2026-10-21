package app.shift.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.shift.learn.data.ContentState
import app.shift.learn.data.Lesson
import app.shift.learn.data.UserPrefs
import app.shift.learn.data.db.Bookmark
import app.shift.learn.data.db.LessonProgress
import app.shift.learn.data.db.QuizAttempt
import app.shift.learn.domain.InterestSelection
import app.shift.learn.domain.QuizResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val c: AppContainer) : ViewModel() {
    val prefs: StateFlow<UserPrefs?> = c.prefs.prefs.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _content = MutableStateFlow<ContentState?>(null)
    val content: StateFlow<ContentState?> = _content.asStateFlow()
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _selection = MutableStateFlow(InterestSelection())
    val selection: StateFlow<InterestSelection> = _selection.asStateFlow()

    val completed: StateFlow<Set<String>> =
        c.db.dao().completedIds().map { it.toSet() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val bookmarks: StateFlow<Set<String>> =
        c.db.dao().bookmarkIds().map { it.toSet() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        viewModelScope.launch {
            try {
                _content.value = c.content.load()
            } catch (e: Exception) {
                _loadError.value = "Bundled content could not be read: ${e.message}"
            }
        }
    }

    fun toggleInterest(id: String) = _selection.update { it.toggle(id) }

    fun finishOnboarding() {
        val s = _selection.value
        if (!s.isComplete) return
        viewModelScope.launch { c.prefs.completeOnboarding(s.selected) }
    }

    fun setMode(mode: String) {
        viewModelScope.launch { c.prefs.setMode(mode) }
    }

    fun recordQuiz(lesson: Lesson, result: QuizResult) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            c.db.dao().insertAttempt(
                QuizAttempt(lessonId = lesson.id, lessonVersion = lesson.version, correct = result.correct,
                    total = result.total, passed = result.passed, takenAt = now)
            )
            if (result.passed) c.db.dao().upsertProgress(LessonProgress(lesson.id, true, now))
        }
    }

    fun toggleBookmark(lessonId: String) {
        viewModelScope.launch {
            if (lessonId in bookmarks.value) c.db.dao().removeBookmark(lessonId)
            else c.db.dao().addBookmark(Bookmark(lessonId, System.currentTimeMillis()))
        }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory { initializer { AppViewModel(c) } }
    }
}
