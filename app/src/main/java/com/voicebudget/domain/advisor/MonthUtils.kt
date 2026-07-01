package com.voicebudget.domain.advisor

import com.voicebudget.domain.model.Transaction
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

internal fun Long.toYearMonth(): YearMonth =
    YearMonth.from(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))

internal fun Transaction.yearMonth(): YearMonth = createdAt.toYearMonth()
