package com.voicebudget.data.repository

import com.voicebudget.data.database.FinancialGoalEntity
import com.voicebudget.domain.goals.FinancialGoal
import java.time.YearMonth

fun FinancialGoalEntity.toDomain(): FinancialGoal = FinancialGoal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    targetMonth = YearMonth.of(targetYear, targetMonth),
    createdAt = createdAt,
)

fun FinancialGoal.toEntity(): FinancialGoalEntity = FinancialGoalEntity(
    id = id,
    name = name,
    targetAmount = targetAmount,
    targetYear = targetMonth.year,
    targetMonth = targetMonth.monthValue,
    createdAt = createdAt,
)
