package com.voicebudget.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.voicebudget.MainActivity
import com.voicebudget.R
import com.voicebudget.utils.formatAmount
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class BalanceGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, BalanceWidgetEntryPoint::class.java)
        val getCombinedBalance = entryPoint.getCombinedBalanceUseCase()
        val observeSecuritySettings = entryPoint.observeSecuritySettingsUseCase()
        val balance = getCombinedBalance().first()
        val isPinSet = observeSecuritySettings().first().isPinSet

        val balanceText = when {
            balance == null -> "--"
            isPinSet -> context.getString(R.string.widget_balance_masked)
            else -> formatAmount(balance.amount, balance.currency.symbol)
        }

        val addTransactionIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_START_VOICE_ENTRY, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            WidgetContent(
                appName = context.getString(R.string.app_name),
                balanceText = balanceText,
                onOpenApp = actionStartActivity(Intent(context, MainActivity::class.java)),
                onAddClick = actionStartActivity(addTransactionIntent),
            )
        }
    }

    companion object {
        suspend fun refreshAll(context: Context) {
            BalanceGlanceWidget().updateAll(context)
        }
    }
}

@Composable
private fun WidgetContent(
    appName: String,
    balanceText: String,
    onOpenApp: Action,
    onAddClick: Action,
) {
    Box(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xFF10B981)))) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .clickable(onOpenApp),
        ) {
            Text(
                text = appName,
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp),
            )
            Text(
                text = balanceText,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.padding(top = 8.dp),
            )
        }
        Box(
            modifier = GlanceModifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF047857)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(ColorProvider(Color.White))
                    .clickable(onAddClick),
            )
        }
    }
}
