package com.voicebudget.di

import com.voicebudget.presentation.voice.AndroidVoiceRecognizerService
import com.voicebudget.presentation.voice.VoiceRecognizerService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds
    abstract fun bindVoiceRecognizerService(impl: AndroidVoiceRecognizerService): VoiceRecognizerService
}
