package com.voicebudget.domain.parser

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.TransactionType
import javax.inject.Inject

/**
 * Rule-based, offline speech-to-transaction parser. No AI/network calls by design (MVP requirement).
 * Supports Russian and English input by matching individual word tokens against [CategoryKeywords].
 */
class TransactionParser @Inject constructor() {

    fun parse(rawText: String): ParseResult {
        val text = rawText.trim()
        if (text.isEmpty()) return ParseResult.Failure(ParseFailureReason.EMPTY_INPUT)

        val amount = AMOUNT_REGEX.find(text)
            ?.value
            ?.let(::normalizeAmount)
            ?.toDoubleOrNull()
            ?: return ParseResult.Failure(ParseFailureReason.AMOUNT_NOT_FOUND)

        val tokens = WORD_REGEX.findAll(text).map { it.value }.toList()
        val lowerTokens = tokens.map { it.lowercase() }

        val incomeIndex = lowerTokens.indexOfFirst { it in CategoryKeywords.incomeKeywords }
        val expenseIndex = lowerTokens.indexOfFirst { it in CategoryKeywords.expenseKeywords }

        val type: TransactionType
        val category: Category
        val matchedToken: String?

        when {
            incomeIndex != -1 -> {
                type = TransactionType.INCOME
                category = CategoryKeywords.incomeKeywords.getValue(lowerTokens[incomeIndex])
                matchedToken = tokens[incomeIndex]
            }
            expenseIndex != -1 -> {
                type = TransactionType.EXPENSE
                category = CategoryKeywords.expenseKeywords.getValue(lowerTokens[expenseIndex])
                matchedToken = tokens[expenseIndex]
            }
            else -> {
                type = TransactionType.EXPENSE
                category = Category.OTHER_EXPENSE
                matchedToken = null
            }
        }

        val description = matchedToken?.capitalizeFirst() ?: fallbackDescription(tokens, lowerTokens)

        return ParseResult.Success(ParsedTransaction(type, category, amount, description))
    }

    private fun fallbackDescription(tokens: List<String>, lowerTokens: List<String>): String {
        val meaningful = tokens.filterIndexed { index, _ -> lowerTokens[index] !in CategoryKeywords.noiseWords }
        val joined = meaningful.joinToString(" ")
        return joined.ifBlank { "Other" }.capitalizeFirst()
    }

    private fun String.capitalizeFirst(): String =
        replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }

    /**
     * Speech recognition renders numbers above 999 with a grouping separator
     * (e.g. "2 000" or "2,000" for "two thousand"; Android's ru-RU number
     * formatting uses U+00A0 non-breaking space), which looks identical to a
     * decimal separator. A trailing group of exactly 3 digits is treated as
     * thousands grouping, repeated for millions/billions; anything shorter is
     * treated as a decimal part.
     */
    private fun normalizeAmount(raw: String): String {
        val groups = raw.split(GROUP_SEPARATOR_REGEX)
        if (groups.size == 1) return groups[0]
        val last = groups.last()
        return if (last.length == 3) groups.joinToString("") else groups.dropLast(1).joinToString("") + "." + last
    }

    private companion object {
        private const val SEPARATOR_CLASS = "[\\s\u00A0.,]"
        val AMOUNT_REGEX = Regex("\\d+(?:$SEPARATOR_CLASS\\d+)*")
        val GROUP_SEPARATOR_REGEX = Regex(SEPARATOR_CLASS)
        val WORD_REGEX = Regex("""\p{L}+""")
    }
}
