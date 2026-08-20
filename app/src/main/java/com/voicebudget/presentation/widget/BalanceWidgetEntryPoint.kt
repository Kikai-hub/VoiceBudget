package com.voicebudget.presentation.widget

import com.voicebudget.domain.usecase.GetCombinedBalanceUseCase
import com.voicebudget.domain.usecase.ObserveSecuritySettingsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * [androidx.glance.appwidget.GlanceAppWidget] isn't an Android-framework class Hilt can inject
 * directly (only its [androidx.glance.appwidget.GlanceAppWidgetReceiver] is), so its data needs
 * are pulled through this manual entry point instead.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BalanceWidgetEntryPoint {
    fun getCombinedBalanceUseCase(): GetCombinedBalanceUseCase
    fun observeSecuritySettingsUseCase(): ObserveSecuritySettingsUseCase
}
