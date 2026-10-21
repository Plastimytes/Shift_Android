package app.shift.learn

import android.content.Context
import app.shift.learn.data.ContentRepository
import app.shift.learn.data.PrefsRepository
import app.shift.learn.data.db.ShiftDatabase

/** Manual dependency container. Chosen over Hilt to keep the first slice small; revisit when modules multiply. */
class AppContainer(context: Context) {
    val content = ContentRepository(context)
    val prefs = PrefsRepository(context)
    val db = ShiftDatabase.build(context)
}
