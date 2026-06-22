package com.voicebudget.domain.usecase

import com.voicebudget.domain.parser.ParseResult
import com.voicebudget.domain.parser.TransactionParser
import javax.inject.Inject

class ParseVoiceInputUseCase @Inject constructor(
    private val parser: TransactionParser,
) {
    operator fun invoke(rawText: String): ParseResult = parser.parse(rawText)
}
