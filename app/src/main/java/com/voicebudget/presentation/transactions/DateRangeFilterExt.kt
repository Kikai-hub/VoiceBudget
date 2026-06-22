package com.voicebudget.presentation.transactions

import java.time.Instant
import java.time.ZoneId

data class TimeRange(val startInclusive: Long, val endExclusive: Long) {
    operator fun contains(epochMillis: Long): Boolean = epochMillis in startInclusive until endExclusive
}

fun DateRangeFilter.toTimeRange(referenceTimeMillis: Long = System.currentTimeMillis()): TimeRange? {
    val zone = ZoneId.systemDefault()
    val now = Instant.ofEpochMilli(referenceTimeMillis).atZone(zone)

    return when (this) {
        DateRangeFilter.ALL_TIME -> null
        DateRangeFilter.THIS_MONTH -> {
            val start = now.toLocalDate().withDayOfMonth(1).atStartOfDay(zone)
            TimeRange(start.toInstant().toEpochMilli(), start.plusMonths(1).toInstant().toEpochMilli())
        }
        DateRangeFilter.LAST_MONTH -> {
            val start = now.toLocalDate().withDayOfMonth(1).minusMonths(1).atStartOfDay(zone)
            TimeRange(start.toInstant().toEpochMilli(), start.plusMonths(1).toInstant().toEpochMilli())
        }
        DateRangeFilter.LAST_7_DAYS -> {
            val end = now.plusDays(1).toLocalDate().atStartOfDay(zone)
            TimeRange(end.minusDays(7).toInstant().toEpochMilli(), end.toInstant().toEpochMilli())
        }
    }
}
