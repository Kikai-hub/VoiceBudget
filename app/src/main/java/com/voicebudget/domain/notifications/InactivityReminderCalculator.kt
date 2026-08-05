package com.voicebudget.domain.notifications

import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Decides whether the user should be nudged to log a transaction, based purely on how long ago
 * their last transaction was recorded — kept free of Android/WorkManager types so it is trivially
 * unit-testable.
 */
class InactivityReminderCalculator @Inject constructor() {

    fun shouldRemind(lastTransactionAtMillis: Long?, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (lastTransactionAtMillis == null) return false
        return nowMillis - lastTransactionAtMillis >= INACTIVITY_THRESHOLD_MILLIS
    }

    companion object {
        const val INACTIVITY_THRESHOLD_DAYS = 3L
        val INACTIVITY_THRESHOLD_MILLIS = TimeUnit.DAYS.toMillis(INACTIVITY_THRESHOLD_DAYS)
    }
}
