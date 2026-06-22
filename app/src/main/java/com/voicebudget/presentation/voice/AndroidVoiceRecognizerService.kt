package com.voicebudget.presentation.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.voicebudget.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AndroidVoiceRecognizerService @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : VoiceRecognizerService {

    override fun listen(languageTag: String): Flow<RecognitionEvent> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(RecognitionEvent.Error(context.getString(R.string.error_recognition_unavailable)))
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(RecognitionEvent.ReadyForSpeech)
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                trySend(RecognitionEvent.Error(describeError(error)))
                close()
            }

            override fun onResults(results: Bundle?) {
                val text = results?.bestMatch().orEmpty()
                trySend(RecognitionEvent.FinalResult(text))
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.bestMatch()
                if (!text.isNullOrBlank()) trySend(RecognitionEvent.PartialResult(text))
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)

        awaitClose { recognizer.destroy() }
    }

    private fun Bundle.bestMatch(): String? =
        getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun describeError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.error_no_speech_match)
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> context.getString(R.string.error_speech_timeout)
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> context.getString(R.string.error_mic_permission)
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> context.getString(R.string.error_network)
        SpeechRecognizer.ERROR_AUDIO -> context.getString(R.string.error_audio)
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> context.getString(R.string.error_recognizer_busy)
        else -> context.getString(R.string.error_recognition_generic, error)
    }
}
