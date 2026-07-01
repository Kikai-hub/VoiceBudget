package com.voicebudget.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voicebudget.domain.advisor.AdvisorSettings
import com.voicebudget.domain.repository.AdvisorSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object AdvisorKeys {
    val SMALL_PURCHASE_THRESHOLD = doublePreferencesKey("advisor_small_purchase_threshold")
    val TOP_CATEGORY_THRESHOLD = doublePreferencesKey("advisor_top_category_threshold_percent")
    val DESIRED_SAVINGS_RATE = doublePreferencesKey("advisor_desired_savings_rate")
    val ANALYSIS_PERIOD_MONTHS = intPreferencesKey("advisor_analysis_period_months")
    val DISMISSED_IDS = stringPreferencesKey("advisor_dismissed_ids")
}

class AdvisorSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : AdvisorSettingsRepository {

    override fun observeSettings(): Flow<AdvisorSettings> = dataStore.data.map { prefs ->
        AdvisorSettings(
            smallPurchaseThreshold = prefs[AdvisorKeys.SMALL_PURCHASE_THRESHOLD] ?: 500.0,
            topCategoryThresholdPercent = prefs[AdvisorKeys.TOP_CATEGORY_THRESHOLD] ?: 30.0,
            desiredSavingsRatePercent = prefs[AdvisorKeys.DESIRED_SAVINGS_RATE] ?: 10.0,
            analysisPeriodMonths = prefs[AdvisorKeys.ANALYSIS_PERIOD_MONTHS] ?: 3,
        )
    }

    override fun observeDismissedIds(): Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[AdvisorKeys.DISMISSED_IDS]
            ?.split(',')
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()
    }

    override suspend fun updateSettings(settings: AdvisorSettings) {
        dataStore.edit { prefs ->
            prefs[AdvisorKeys.SMALL_PURCHASE_THRESHOLD] = settings.smallPurchaseThreshold
            prefs[AdvisorKeys.TOP_CATEGORY_THRESHOLD] = settings.topCategoryThresholdPercent
            prefs[AdvisorKeys.DESIRED_SAVINGS_RATE] = settings.desiredSavingsRatePercent
            prefs[AdvisorKeys.ANALYSIS_PERIOD_MONTHS] = settings.analysisPeriodMonths
        }
    }

    override suspend fun dismissAdvice(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[AdvisorKeys.DISMISSED_IDS]
                ?.split(',')
                ?.filter { it.isNotBlank() }
                ?.toMutableSet()
                ?: mutableSetOf()
            current.add(id)
            prefs[AdvisorKeys.DISMISSED_IDS] = current.joinToString(",")
        }
    }

    override suspend fun restoreAdvice(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[AdvisorKeys.DISMISSED_IDS]
                ?.split(',')
                ?.filter { it.isNotBlank() }
                ?.toMutableSet()
                ?: return@edit
            current.remove(id)
            prefs[AdvisorKeys.DISMISSED_IDS] = current.joinToString(",")
        }
    }
}
