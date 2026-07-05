package com.voicebudget.fakes

import android.content.Context
import com.voicebudget.R
import io.mockk.every
import io.mockk.mockk
import java.util.Locale

/**
 * Real strings.xml (EN) templates keyed by resource id, so generator unit tests can assert
 * on formatted output without pulling in Robolectric just to resolve resources.
 */
private val templates: Map<Int, String> = mapOf(
    R.string.advice_high_spending_title to "High spending this month",
    R.string.advice_high_spending_desc to "You spent %1\$d%% more this month than last month.",
    R.string.advice_category_growth_title to "%1\$s expenses grew",
    R.string.advice_category_growth_desc to "%1\$s expenses increased by %2\$d%%.",
    R.string.advice_small_purchases_title to "Many small purchases",
    R.string.advice_small_purchases_desc to "You made %1\$d purchases under %2\$d this month.",
    R.string.advice_savings_low_title to "Low savings rate",
    R.string.advice_savings_low_desc to
        "Your savings rate is %1\$d%% — below your %2\$d%% goal. Try reducing expenses by %3\$d.",
    R.string.advice_savings_positive_title to "Great savings!",
    R.string.advice_savings_positive_desc to "You saved %1\$d%% of your income this month. Keep it up!",
    R.string.advice_recurring_title to "Recurring payment detected",
    R.string.advice_recurring_desc to
        "You have a recurring %1\$s payment of ~%2\$d for %3\$d consecutive months.",
    R.string.advice_income_trend_title to "Declining income",
    R.string.advice_income_trend_desc to
        "Your income has been declining for %1\$d consecutive months (%2\$d%% change).",
    R.string.advice_monthly_summary_title to "Monthly summary",
    R.string.advice_monthly_summary_base to "Income: %1\$d, Expenses: %2\$d.",
    R.string.advice_monthly_summary_top_category to "Largest category: %1\$s (%2\$d).",
    R.string.advice_monthly_summary_save_more to "Try to save more next month.",
    R.string.advice_top_category_title to "Biggest expense: %1\$s",
    R.string.advice_top_category_desc to
        "%1\$s takes %2\$d%% of your budget. Reducing it by %3\$d%% could save ~%4\$d per year.",
    R.string.category_food to "Food",
    R.string.category_cafe to "Cafe",
    R.string.category_transport to "Transport",
    R.string.category_shopping to "Shopping",
    R.string.category_health to "Health",
    R.string.category_utilities to "Utilities",
    R.string.category_entertainment to "Entertainment",
    R.string.category_other to "Other",
    R.string.category_salary to "Salary",
    R.string.category_freelance to "Freelance",
    R.string.category_bonus to "Bonus",
    R.string.category_gift to "Gift",
    R.string.goal_on_track_desc to
        "At your current pace (~%4\$d/month) you're on track to reach \"%1\$s\" (%2\$d) by %3\$s.",
    R.string.goal_off_track_desc to
        "To reach \"%1\$s\" (%2\$d) by %3\$s you need to save ~%4\$d/month — your current pace is ~%5\$d/month.",
    R.string.goal_suggestion_desc to "Try cutting %1\$s spending by %2\$d%% (~%3\$d/month) to help close the gap.",
    R.string.error_goal_invalid_name to "Please enter a goal name.",
    R.string.error_goal_invalid_amount to "Please enter a valid target amount.",
    R.string.error_goal_deadline_past to "Deadline must be in the future.",
)

fun fakeAndroidContext(): Context {
    val context = mockk<Context>()
    every { context.getString(any()) } answers { templates.getValue(firstArg()) }
    every { context.getString(any(), *anyVararg()) } answers {
        val resId: Int = firstArg()
        @Suppress("UNCHECKED_CAST")
        val formatArgs = invocation.args[1] as Array<Any?>
        String.format(Locale.US, templates.getValue(resId), *formatArgs)
    }
    return context
}
