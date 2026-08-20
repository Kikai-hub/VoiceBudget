package com.voicebudget.domain.usecase

import com.voicebudget.domain.model.CategoryBudgetLimit
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.domain.model.matches
import java.time.YearMonth

/** Month-to-date spend counted against [limit], shared by the progress display and the alert sweep. */
internal fun List<Transaction>.spentAgainst(limit: CategoryBudgetLimit, referenceMonth: YearMonth): Double =
    filter { it.type == TransactionType.EXPENSE && it.isInMonth(referenceMonth) && limit.matches(it) }
        .sumOf { it.amount }
