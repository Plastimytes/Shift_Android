package app.shift.learn.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.shift.learn.domain.InterestSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "shift_prefs")

data class UserPrefs(
    val onboardingDone: Boolean = false,
    val interests: Set<String> = emptySet(),
    val mode: String = "easy",
)

class PrefsRepository(private val context: Context) {
    private object Keys {
        val done = booleanPreferencesKey("onboarding_done")
        val interests = stringSetPreferencesKey("interests")
        val mode = stringPreferencesKey("mode")
    }

    val prefs: Flow<UserPrefs> = context.dataStore.data.map {
        UserPrefs(it[Keys.done] ?: false, it[Keys.interests] ?: emptySet(), it[Keys.mode] ?: "easy")
    }

    /** Refuses to save unless exactly three interests were chosen. */
    suspend fun completeOnboarding(interests: Set<String>) {
        require(interests.size == InterestSelection.REQUIRED) { "Exactly ${InterestSelection.REQUIRED} interests required" }
        context.dataStore.edit {
            it[Keys.interests] = interests
            it[Keys.done] = true
        }
    }

    suspend fun setMode(mode: String) {
        context.dataStore.edit { it[Keys.mode] = mode }
    }
}
