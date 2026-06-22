package com.voicebudget.presentation.voice

import kotlinx.coroutines.flow.Flow

sealed interface RecognitionEvent {
    data object ReadyForSpeech : RecognitionEvent
    data class PartialResult(val text: String) : RecognitionEvent
    data class FinalResult(val text: String) : RecognitionEvent
    data class Error(val message: String) : RecognitionEvent
}

interface VoiceRecognizerService {
    /** Starts a single listening session. The returned flow completes after a final result or error. */
    fun listen(languageTag: String): Flow<RecognitionEvent>
}
