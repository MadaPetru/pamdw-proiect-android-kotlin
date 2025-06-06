package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Operation
import ro.adi.agroadmin.data.OperationChartDto
import ro.adi.agroadmin.service.CurrencyApi

class HomeActivity : AppCompatActivity() {

    private lateinit var fieldsChart: LineChart
    private lateinit var operationsChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val currencySpinnerFields = findViewById<Spinner>(R.id.currencySpinnerFields)
        val currencySpinnerOperations = findViewById<Spinner>(R.id.currencySpinnerOperations)

        currencySpinnerFields.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCurrency = parent.getItemAtPosition(position).toString()
                updateFieldsChart(selectedCurrency)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        currencySpinnerOperations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCurrency = parent.getItemAtPosition(position).toString()
                updateOperationsChart(selectedCurrency)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        fieldsChart = findViewById(R.id.fieldsChart)
        operationsChart = findViewById(R.id.operationsChart)

        setupChart(fieldsChart, listOf("B", "Test"), listOf(200f, 2800f), listOf(200f, 3600f))
       // setupChart(operationsChart, listOf("A", "semănat", "Arat"), listOf(150f, 3200f, 200f), listOf(100f, 3300f, 180f))

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_fields -> {
                    startActivity(Intent(this, FieldsActivity::class.java))
                    true
                }
                R.id.nav_home -> true // Already on Home
                else -> false
            }
        }
    }

    private suspend fun fetchOperationsSafe(): List<OperationChartDto> = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val username = FirebaseAuth.getInstance().currentUser?.email ?: return@withContext emptyList()

        val operationsList = mutableListOf<OperationChartDto>()

        val fieldDocs = db.collection("users").document(username).collection("fields").get().await()
        Log.e("FieldDocs", "Fetched ${fieldDocs.size()} fields for user: $username")
        for (fieldDoc in fieldDocs) {
            val fieldName = fieldDoc.getString("name") ?: fieldDoc.id
            val opsSnap = db.collection("users").document(username)
                .collection("fields").document(fieldDoc.id)
                .collection("operations")
                .get()
                .await()

            Log.e("OpsSnap", "Fetched ${opsSnap.size()} operations for field: $fieldName")

            for (doc in opsSnap) {
                val type = doc.getString("type") ?: ""
                val cost = doc.getDouble("cost") ?: 0.0
                val revenue = doc.getDouble("revenue") ?: 0.0
                val currency = doc.getString("currency") ?: "RON"
                operationsList.add(OperationChartDto(type, cost,"","", revenue, fieldName, currency))
            }
        }

        return@withContext operationsList
    }


    private fun prepareChartData(
        operations: List<OperationChartDto>,
        groupByField: Boolean,
        rate: Float
    ): Triple<List<String>, List<Float>, List<Float>> {
        val groupMap = mutableMapOf<String, Pair<Float, Float>>() // Label -> (cost, revenue)

        for (op in operations) {
            val key = if (groupByField) op.fieldName else op.type
            val current = groupMap.getOrDefault(key, 0f to 0f)
            val newCost = current.first + (op.cost * rate).toFloat()
            val newRevenue = current.second + (op.revenue * rate).toFloat()
            groupMap[key] = newCost to newRevenue
        }

        val labels = groupMap.keys.toList()
        val costData = labels.map { groupMap[it]?.first ?: 0f }
        val revenueData = labels.map { groupMap[it]?.second ?: 0f }

        return Triple(labels, costData, revenueData)
    }



    private fun updateFieldsChart(currencyTo: String) {
        val baseCurrency = "RON" // or whatever your original values are in
        lifecycleScope.launch {
            val rates = CurrencyApi.getRates(this@HomeActivity, baseCurrency, currencyTo)
            val rate = rates[currencyTo] ?: 1.0
            val updatedCost = listOf(200f, 2800f).map { it * rate.toFloat() }
            val updatedRevenue = listOf(200f, 3600f).map { it * rate.toFloat() }
            setupChart(fieldsChart, listOf("B", "Test"), updatedCost, updatedRevenue)
        }
    }

    private fun updateOperationsChart(currencyTo: String) {
        lifecycleScope.launch {
            try {
                val operations = fetchOperationsSafe()
                Log.d("ChartUpdate", "Fetched ${operations.size} operations")

                if (operations.isNotEmpty()) {
                    // Extract all currencies from operations
                    val currencies = operations.map { it.currency }.toSet()

                    Log.d("Currencies", "Currencies found: $currencies")
                    Log.d("CurrencyTo", "Converting to: $currencyTo")

                    // Fetch rates for each currency -> currencyTo
                    val rates = mutableMapOf<String, Float>()

                    // Fetch all rates concurrently or sequentially:
                    for (currency in currencies) {
                        if (currency == currencyTo) {
                            rates[currency] = 1.0f
                        } else {
                            val apiRates = withContext(Dispatchers.IO) {
                                CurrencyApi.getRates(this@HomeActivity, currency, currencyTo)
                            }
                            rates[currency] = (apiRates[currencyTo] ?: 1.0).toFloat()
                        }
                    }

                    Log.d("ChartUpdate", "Rates map: $rates")

                    // Now prepare chart data with rates map
                    val (labels, costData, revenueData) = prepareChartDataMultiRate(
                        operations,
                        groupByField = false,
                        ratesMap = rates
                    )
                    setupChart(operationsChart, labels, costData, revenueData)
                }
            } catch (e: Exception) {
                Log.e("ChartUpdate", "Failed to update operations chart", e)
            }
        }
    }

    private fun prepareChartDataMultiRate(
        operations: List<OperationChartDto>,
        groupByField: Boolean,
        ratesMap: Map<String, Float>
    ): Triple<List<String>, List<Float>, List<Float>> {
        val groupMap = mutableMapOf<String, Pair<Float, Float>>() // Label -> (cost, revenue)

        for (op in operations) {
            val key = if (groupByField) op.fieldName else op.type
            val current = groupMap.getOrDefault(key, 0f to 0f)

            // Get rate for the operation's currency (default 1f if missing)
            val rate = ratesMap[op.currency] ?: 1.0f

            val newCost = current.first + (op.cost * rate)
            val newRevenue = current.second + (op.revenue * rate)
            groupMap[key] = newCost.toFloat() to newRevenue.toFloat()
        }

        val labels = groupMap.keys.toList()
        val costData = labels.map { groupMap[it]?.first ?: 0f }
        val revenueData = labels.map { groupMap[it]?.second ?: 0f }

        return Triple(labels, costData, revenueData)
    }

    private fun setupChart(chart: LineChart, labels: List<String>, costValues: List<Float>, revenueValues: List<Float>) {
        val costEntries = costValues.mapIndexed { i, value -> Entry(i.toFloat(), value) }
        val revenueEntries = revenueValues.mapIndexed { i, value -> Entry(i.toFloat(), value) }

        val costDataSet = LineDataSet(costEntries, "Cost").apply {
            color = ColorTemplate.COLORFUL_COLORS[3]
            circleRadius = 5f
            setCircleColor(color)
            lineWidth = 2f
        }

        val revenueDataSet = LineDataSet(revenueEntries, "Revenue").apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            circleRadius = 5f
            setCircleColor(color)
            lineWidth = 2f
        }

        chart.data = LineData(costDataSet, revenueDataSet)
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        chart.axisRight.isEnabled = false

        val leftAxis: YAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)

        val legend: Legend = chart.legend
        legend.isEnabled = true

        chart.invalidate()
    }
}
