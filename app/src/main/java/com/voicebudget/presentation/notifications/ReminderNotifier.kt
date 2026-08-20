package com.voicebudget.presentation.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.voicebudget.MainActivity
import com.voicebudget.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Shows the "you've been inactive" reminder notification prompting the user to log transactions. */
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showInactivityReminder() {
        ensureChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(context.getString(R.string.notification_reminder_text))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun showBudgetLimitApproaching(categoryLabel: String, spentText: String, limitText: String) {
        showBudgetLimitAlert(
            title = context.getString(R.string.notification_budget_limit_approaching_title),
            text = context.getString(R.string.notification_budget_limit_approaching_text, spentText, limitText, categoryLabel),
            notificationId = BUDGET_NOTIFICATION_ID_BASE + categoryLabel.hashCode(),
        )
    }

    fun showBudgetLimitExceeded(categoryLabel: String, spentText: String, limitText: String) {
        showBudgetLimitAlert(
            title = context.getString(R.string.notification_budget_limit_exceeded_title),
            text = context.getString(R.string.notification_budget_limit_exceeded_text, spentText, limitText, categoryLabel),
            notificationId = BUDGET_NOTIFICATION_ID_BASE + categoryLabel.hashCode(),
        )
    }

    private fun showBudgetLimitAlert(title: String, text: String, notificationId: Int) {
        ensureBudgetLimitChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun ensureBudgetLimitChannel() {
        val channel = NotificationChannel(
            BUDGET_CHANNEL_ID,
            context.getString(R.string.notification_budget_limit_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_budget_limit_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "transaction_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val BUDGET_CHANNEL_ID = "budget_limit_alerts"
        private const val BUDGET_NOTIFICATION_ID_BASE = 2000
    }
}
