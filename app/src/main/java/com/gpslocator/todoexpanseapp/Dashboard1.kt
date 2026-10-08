package com.gpslocator.todoexpanseapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.Expense
import com.ExpenseAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.tabs.TabLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth

class Dashboard1 : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()
    private lateinit var tvTotalAmount: TextView


    private val addExpenseLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {
                loadTodayExpenses()   // Just reload from Room
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSkipped = sharedPref.getBoolean("is_skipped", false)

        // Only redirect to Login if NO user is logged in AND they haven't clicked "Skip"
        if (user == null && !isSkipped) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.dashboard1)

        // Trigger sync if user is logged in
        if (user != null) {
            scheduleSync()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token)
            }
        }

        tvTotalAmount = findViewById(R.id.tvTotalAmount)

        recycler = findViewById(R.id.recyclerExpenses)
        adapter = ExpenseAdapter(expenseList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_add -> {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    addExpenseLauncher.launch(intent)
                    true
                }

                R.id.nav_stats -> {
                    val intent = Intent(this, InsightsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> true
            }
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {

                    0 -> loadTodayExpenses()
                    1 -> loadWeekExpenses()
                    2 -> loadMonthExpenses()
                    3 -> loadAllExpenses()   // Calendar or All
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        loadTodayExpenses()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(syncRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("NOTIFICATION", "Permission Granted")
            } else {
                Log.d("NOTIFICATION", "Permission Denied")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTodayExpenses()
    }

    private fun updateTotalAmount() {

        var total = 0.0

        for (expense in expenseList) {
            total += expense.amount.toDouble()
        }

        tvTotalAmount.text = "₹%.2f".format(total)
    }

    private fun loadTodayExpenses() {

        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {

            val expensesFromDb = db.expenseDao().getTodayExpenses()

            runOnUiThread {

                expenseList.clear()

                for (expense in expensesFromDb) {

                    val formattedDate =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(expense.timestamp))

                    expenseList.add(
                        Expense(
                            expense.title,
                            formattedDate,
                            expense.amount.toString(),
                            getIconByCategory(expense.category)
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                updateTotalAmount()
            }
        }
    }

    private fun loadWeekExpenses() {

        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {

            val expensesFromDb = db.expenseDao().getThisWeekExpenses()

            runOnUiThread {
                updateUI(expensesFromDb)
            }
        }
    }

    private fun loadMonthExpenses() {

        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {

            val expensesFromDb = db.expenseDao().getThisMonthExpenses()

            runOnUiThread {
                updateUI(expensesFromDb)
            }
        }
    }

    private fun loadAllExpenses() {

        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {

            val expensesFromDb = db.expenseDao().getAllExpenses()

            runOnUiThread {
                updateUI(expensesFromDb)
            }
        }
    }

    private fun updateUI(expensesFromDb: List<ExpenseEntity>) {

        expenseList.clear()

        for (expense in expensesFromDb) {

            val formattedDate =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(expense.timestamp))

            expenseList.add(
                Expense(
                    expense.title,
                    formattedDate,
                    expense.amount.toString(),
                    getIconByCategory(expense.category)
                )
            )
        }

        adapter.notifyDataSetChanged()
        updateTotalAmount()
    }

    private fun getIconByCategory(category: String): Int {
        return when (category) {
            "Food" -> R.drawable.fastfood
            "Travel" -> R.drawable.electric_rickshaw
            "Shopping" -> R.drawable.shopping_bag
            "Misc" -> R.drawable.currency_rupee
            else -> R.drawable.currency_rupee
        }
    }
}
