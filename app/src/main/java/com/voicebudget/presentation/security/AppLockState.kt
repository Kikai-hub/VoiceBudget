package com.voicebudget.presentation.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.voicebudget.domain.repository.SecurityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the app should show the PIN/biometric lock screen: on cold start, and again
 * after returning from the background past [LOCK_GRACE_PERIOD_MILLIS] (so brief backgrounding —
 * e.g. a permission dialog, switching to copy a number — doesn't force a re-unlock every time).
 *
 * [isLocked] starts `null` ("still checking") rather than `false`, so callers can hold the UI on
 * a blank/loading frame until the PIN-set check resolves — defaulting to `false` would let a
 * locked user's financial data flash on screen for one frame before the lock screen appears.
 */
@Singleton
class AppLockState @Inject constructor(
    private val securityRepository: SecurityRepository,
) : DefaultLifecycleObserver {

    private val _isLocked = MutableStateFlow<Boolean?>(null)
    val isLocked: StateFlow<Boolean?> = _isLocked

    private var backgroundedAt: Long? = null
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    override fun onStart(owner: LifecycleOwner) {
        val elapsedSinceBackground = backgroundedAt?.let { System.currentTimeMillis() - it }
        val isColdStart = backgroundedAt == null
        val pastGracePeriod = elapsedSinceBackground != null && elapsedSinceBackground >= LOCK_GRACE_PERIOD_MILLIS
        if (isColdStart || pastGracePeriod) {
            if (isColdStart) _isLocked.value = null
            scope.launch {
                _isLocked.value = securityRepository.isPinSet().first()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAt = System.currentTimeMillis()
    }

    fun unlock() {
        _isLocked.value = false
    }

    private companion object {
        const val LOCK_GRACE_PERIOD_MILLIS = 30_000L
    }
}
