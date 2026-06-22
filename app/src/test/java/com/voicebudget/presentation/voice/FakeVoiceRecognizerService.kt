package com.voicebudget.presentation.voice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeVoiceRecognizerService(private val events: List<RecognitionEvent>) : VoiceRecognizerService {

    var lastLanguageTag: String? = null
        private set

    override fun listen(languageTag: String): Flow<RecognitionEvent> {
        lastLanguageTag = languageTag
        return events.asFlow()
    }
}
