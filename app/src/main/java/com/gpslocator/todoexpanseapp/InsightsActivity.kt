package com.gpslocator.todoexpanseapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsightsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var tvTopCategory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarInsights)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_stats
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Dashboard1::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddExpenseActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_stats -> true
                else -> false
            }
        }

        pieChart = findViewById(R.id.pieChart)
        tvTopCategory = findViewById(R.id.tvTopCategory)

        loadInsights()
    }

    private fun loadInsights() {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val allExpenses = db.expenseDao().getAllExpenses()
            
            val categoryMap = mutableMapOf<String, Double>()
            for (expense in allExpenses) {
                categoryMap[expense.category] = (categoryMap[expense.category] ?: 0.0) + expense.amount
            }

            val entries = ArrayList<PieEntry>()
            var topCategory = "None"
            var maxAmount = 0.0

            for ((category, amount) in categoryMap) {
                entries.add(PieEntry(amount.toFloat(), category))
                if (amount > maxAmount) {
                    maxAmount = amount
                    topCategory = category
                }
            }

            withContext(Dispatchers.Main) {
                updateChart(entries)
                tvTopCategory.text = "$topCategory (₹${String.format("%.2f", maxAmount)})"
            }
        }
    }

    private fun updateChart(entries: ArrayList<PieEntry>) {
        val dataSet = PieDataSet(entries, "Expense Categories")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toMutableList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Expenses"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
