package com.voicebudget.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * AppCompat's per-app-language backport (used on API < 33) only patches the [Context] of live
 * Activities on recreate — an injected `@ApplicationContext`/widget/worker [Context] keeps
 * resolving strings in the device's system locale even after the user picks a different app
 * language. Call this before [Context.getString] on any such context so it honors the
 * user-selected language too.
 */
fun Context.withAppLocale(): Context {
    val locale = currentAppLocale() ?: return this
    val configuration = Configuration(resources.configuration).apply { setLocale(locale) }
    return createConfigurationContext(configuration)
}

/**
 * The user-selected app language, or null if none was set yet (device default applies).
 * Same rationale as [withAppLocale]: `Locale.getDefault()` isn't kept in sync with the app's
 * per-app language pick on API < 33, so code formatting dates/names for display should prefer
 * this over the JVM default.
 */
fun currentAppLocale(): Locale? = AppCompatDelegate.getApplicationLocales().get(0)
