package com.voicebudget.data.csv

import com.voicebudget.domain.model.Category
import com.voicebudget.domain.model.Transaction
import com.voicebudget.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Plain Kotlin (no Android dependency) CSV (de)serializer for transactions, so it can be
 * unit-tested without an Android runtime and reused by both export and import flows.
 */
object TransactionCsv {

    private const val HEADER = "Date,Type,Category,Description,Amount"
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun toCsv(transactions: List<Transaction>): String {
        val rows = transactions.joinToString(separator = "\n") { toRow(it) }
        return if (rows.isEmpty()) HEADER else "$HEADER\n$rows"
    }

    fun fromCsv(content: String): List<Transaction> = content
        .lineSequence()
        .drop(1) // header
        .filter { it.isNotBlank() }
        .mapNotNull { runCatching { parseRow(it) }.getOrNull() }
        .toList()

    private fun toRow(transaction: Transaction): String {
        val date = Instant.ofEpochMilli(transaction.createdAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return listOf(
            date.format(DATE_FORMATTER),
            transaction.type.name,
            transaction.category.name,
            escape(transaction.description),
            String.format(Locale.US, "%.2f", transaction.amount),
        ).joinToString(",")
    }

    private fun parseRow(row: String): Transaction {
        val fields = splitCsvLine(row)
        require(fields.size == 5) { "Expected 5 CSV columns, got ${fields.size}" }
        val (dateText, typeText, categoryText, description, amountText) = fields
        val createdAt = LocalDateTime.parse(dateText, DATE_FORMATTER)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return Transaction(
            amount = amountText.toDouble(),
            type = TransactionType.valueOf(typeText),
            category = Category.valueOf(categoryText),
            description = description,
            createdAt = createdAt,
        )
    }

    private fun escape(value: String): String =
        if (value.contains(',') || value.contains('"')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }

    private fun splitCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    fields += current.toString()
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields += current.toString()
        return fields
    }
}

private operator fun <T> List<T>.component4(): T = this[3]
private operator fun <T> List<T>.component5(): T = this[4]
