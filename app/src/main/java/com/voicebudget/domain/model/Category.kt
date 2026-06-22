package com.voicebudget.domain.model

enum class Category(val type: TransactionType) {
    FOOD(TransactionType.EXPENSE),
    CAFE(TransactionType.EXPENSE),
    TRANSPORT(TransactionType.EXPENSE),
    SHOPPING(TransactionType.EXPENSE),
    HEALTH(TransactionType.EXPENSE),
    UTILITIES(TransactionType.EXPENSE),
    ENTERTAINMENT(TransactionType.EXPENSE),
    OTHER_EXPENSE(TransactionType.EXPENSE),

    SALARY(TransactionType.INCOME),
    FREELANCE(TransactionType.INCOME),
    BONUS(TransactionType.INCOME),
    GIFT(TransactionType.INCOME),
    OTHER_INCOME(TransactionType.INCOME);

    companion object {
        fun forType(type: TransactionType): List<Category> = entries.filter { it.type == type }

        fun other(type: TransactionType): Category =
            if (type == TransactionType.EXPENSE) OTHER_EXPENSE else OTHER_INCOME
    }
}
