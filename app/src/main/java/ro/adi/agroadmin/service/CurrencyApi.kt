package ro.adi.agroadmin.service

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CurrencyApi {

    private const val BASE_URL = "https://api.freecurrencyapi.com/v1/latest"
    private const val API_KEY = "fca_live_cxRniqpO5459O94FMpKr0C4nq9Jw7hX3PjRz4rBP"
    private const val CACHE_DURATION = 12 * 60 * 60 * 1000 // 12 hours

    suspend fun getRates(context: Context, currencyFrom: String, currencyTo: String): Map<String, Double> {
        val cacheKey = "rates_${currencyFrom}_$currencyTo"
        val sharedPrefs = context.getSharedPreferences("CurrencyCache", Context.MODE_PRIVATE)

        try {
            // Try to load from cache
            val cachedData = sharedPrefs.getString(cacheKey, null)
            val cachedTime = sharedPrefs.getLong("${cacheKey}_timestamp", 0)

            if (cachedData != null && System.currentTimeMillis() - cachedTime < CACHE_DURATION) {
                val json = JSONObject(cachedData)
                return json.keys().asSequence().associateWith { json.getDouble(it) }
            }
        } catch (e: Exception) {
            Log.w("CurrencyApi", "Cache error: ${e.message}")
        }

        // Fallback to default
        val defaultRates = mapOf(
            currencyFrom to 1.0,
            currencyTo to 5.0 // Example RON/EUR default
        )

        return try {
            val urlString = "$BASE_URL?apikey=$API_KEY&currencies=$currencyFrom,$currencyTo"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonData = JSONObject(response).getJSONObject("data")
                val rates = jsonData.keys().asSequence().associateWith { jsonData.getDouble(it) }

                // Save to cache
                sharedPrefs.edit()
                    .putString(cacheKey, jsonData.toString())
                    .putLong("${cacheKey}_timestamp", System.currentTimeMillis())
                    .apply()

                rates
            } else {
                Log.e("CurrencyApi", "API error: ${conn.responseCode}")
                defaultRates
            }
        } catch (e: Exception) {
            Log.e("CurrencyApi", "Fetch failed: ${e.message}")
            defaultRates
        }
    }
}
