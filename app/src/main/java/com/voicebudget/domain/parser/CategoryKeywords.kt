package com.voicebudget.domain.parser

import com.voicebudget.domain.model.Category

/**
 * Bilingual (RU/EN) keyword -> category dictionary used by [TransactionParser].
 * Keys are lower-cased single words; multi-word phrases are not supported on purpose
 * to keep matching a simple per-token lookup.
 */
internal object CategoryKeywords {

    val incomeKeywords: Map<String, Category> = buildMap {
        put("salary", Category.SALARY)
        put("зарплата", Category.SALARY)
        put("зп", Category.SALARY)

        put("freelance", Category.FREELANCE)
        put("фриланс", Category.FREELANCE)

        put("bonus", Category.BONUS)
        put("бонус", Category.BONUS)
        put("премия", Category.BONUS)

        put("gift", Category.GIFT)
        put("подарок", Category.GIFT)

        put("income", Category.OTHER_INCOME)
        put("доход", Category.OTHER_INCOME)
    }

    val expenseKeywords: Map<String, Category> = buildMap {
        put("coffee", Category.CAFE)
        put("кофе", Category.CAFE)
        put("cafe", Category.CAFE)
        put("кафе", Category.CAFE)
        put("restaurant", Category.CAFE)
        put("ресторан", Category.CAFE)

        put("food", Category.FOOD)
        put("еда", Category.FOOD)
        put("groceries", Category.FOOD)
        put("продукты", Category.FOOD)

        put("taxi", Category.TRANSPORT)
        put("такси", Category.TRANSPORT)
        put("uber", Category.TRANSPORT)
        put("убер", Category.TRANSPORT)
        put("transport", Category.TRANSPORT)
        put("транспорт", Category.TRANSPORT)

        put("shopping", Category.SHOPPING)
        put("покупки", Category.SHOPPING)
        put("шоппинг", Category.SHOPPING)

        put("medicine", Category.HEALTH)
        put("лекарства", Category.HEALTH)
        put("аптека", Category.HEALTH)

        put("rent", Category.UTILITIES)
        put("аренда", Category.UTILITIES)
        put("квартплата", Category.UTILITIES)
        put("internet", Category.UTILITIES)
        put("интернет", Category.UTILITIES)

        put("entertainment", Category.ENTERTAINMENT)
        put("развлечения", Category.ENTERTAINMENT)
        put("кино", Category.ENTERTAINMENT)
        put("cinema", Category.ENTERTAINMENT)
        put("movie", Category.ENTERTAINMENT)
    }

    val noiseWords: Set<String> = setOf(
        "rubles", "ruble", "rub", "руб", "рублей", "рубля", "рубль",
        "dollars", "dollar", "usd",
        "euro", "euros", "eur",
        "thousand", "тысяча", "тысячи", "тысяч",
        "million", "миллион", "миллиона", "миллионов",
        "billion", "миллиард", "миллиарда", "миллиардов",
    )
}
