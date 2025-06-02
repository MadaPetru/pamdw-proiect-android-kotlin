package ro.adi.agroadmin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import ro.adi.agroadmin.R

class HomeActivity : AppCompatActivity() {

    private lateinit var fieldsChart: LineChart
    private lateinit var operationsChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fieldsChart = findViewById(R.id.fieldsChart)
        operationsChart = findViewById(R.id.operationsChart)

        setupChart(fieldsChart, listOf("B", "Test"), listOf(200f, 2800f), listOf(200f, 3600f))
        setupChart(operationsChart, listOf("A", "semănat", "Arat"), listOf(150f, 3200f, 200f), listOf(100f, 3300f, 180f))
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
