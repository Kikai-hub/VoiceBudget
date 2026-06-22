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

        val amountMatch = AMOUNT_REGEX.find(text)
            ?: return ParseResult.Failure(ParseFailureReason.AMOUNT_NOT_FOUND)
        val baseAmount = normalizeAmount(amountMatch.value).toDoubleOrNull()
            ?: return ParseResult.Failure(ParseFailureReason.AMOUNT_NOT_FOUND)
        val amount = baseAmount * scaleAfter(text, amountMatch.range.last + 1)

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

    /**
     * Speech recognition often spells out the magnitude of large numbers as a word
     * instead of digits (e.g. "2 million" / "2 миллиона" rather than "2000000").
     * If the word immediately following the matched amount is a known scale word,
     * its multiplier is applied to the amount.
     */
    private fun scaleAfter(text: String, fromIndex: Int): Double {
        if (fromIndex > text.length) return 1.0
        val nextWord = SCALE_LOOKAHEAD_REGEX.find(text.substring(fromIndex)) ?: return 1.0
        return SCALE_WORDS[nextWord.groupValues[1].lowercase()] ?: 1.0
    }

    private companion object {
        private const val SEPARATOR_CLASS = "[\\s\u00A0.,]"
        val AMOUNT_REGEX = Regex("\\d+(?:$SEPARATOR_CLASS\\d+)*")
        val GROUP_SEPARATOR_REGEX = Regex(SEPARATOR_CLASS)
        val WORD_REGEX = Regex("""\p{L}+""")
        val SCALE_LOOKAHEAD_REGEX = Regex("""^[\s\u00A0]*(\p{L}+)""")

        val SCALE_WORDS: Map<String, Double> = mapOf(
            "thousand" to 1_000.0,
            "тысяча" to 1_000.0,
            "тысячи" to 1_000.0,
            "тысяч" to 1_000.0,
            "million" to 1_000_000.0,
            "миллион" to 1_000_000.0,
            "миллиона" to 1_000_000.0,
            "миллионов" to 1_000_000.0,
            "billion" to 1_000_000_000.0,
            "миллиард" to 1_000_000_000.0,
            "миллиарда" to 1_000_000_000.0,
            "миллиардов" to 1_000_000_000.0,
        )
    }
}
