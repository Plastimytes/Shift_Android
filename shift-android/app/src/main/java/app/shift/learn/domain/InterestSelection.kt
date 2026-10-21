package app.shift.learn.domain

/** Onboarding rule: the learner must hold exactly three interests before continuing. Immutable. */
data class InterestSelection(val selected: Set<String> = emptySet()) {
    val remaining: Int get() = REQUIRED - selected.size
    val isComplete: Boolean get() = selected.size == REQUIRED

    /** Removing is always allowed. Adding is ignored once three are chosen. */
    fun toggle(id: String): InterestSelection = when {
        id in selected -> InterestSelection(selected - id)
        selected.size < REQUIRED -> InterestSelection(selected + id)
        else -> this
    }

    companion object {
        const val REQUIRED = 3
    }
}
