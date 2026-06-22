package com.voicebudget.domain.model

data class AppSettings(
    val currency: Currency = Currency.RUB,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val recognitionLanguageTag: String = "ru-RU",
)
