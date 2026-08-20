package com.voicebudget.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Talks to the free, no-key "Open Access" exchange rate endpoint. Base currency is fixed to USD;
 * cross rates between any two supported currencies are derived from the two USD rates.
 */
class ExchangeRateApiClient @Inject constructor() {

    suspend fun fetchRatesToUsd(): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(BASE_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            try {
                check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                    "Unexpected response code: ${connection.responseCode}"
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                check(json.optString("result") == "success") { "API reported failure" }
                val ratesJson = json.getJSONObject("rates")
                buildMap {
                    ratesJson.keys().forEach { code -> put(code, ratesJson.getDouble(code)) }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private companion object {
        const val BASE_URL = "https://open.er-api.com/v6/latest/USD"
        const val TIMEOUT_MS = 10_000
    }
}
