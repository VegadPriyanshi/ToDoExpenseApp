package com.gpslocator.todoexpanseapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddExpenseActivity : AppCompatActivity() {

    private var selectedTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAddExpense)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_add
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Dashboard1::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> true
                R.id.nav_stats -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        val etAmount = findViewById<TextInputEditText>(R.id.etAmount)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val btnAddExpense = findViewById<MaterialButton>(R.id.btnAddExpense)

        // Setup Category Dropdown
        val autoCompleteCategory = findViewById<AutoCompleteTextView>(R.id.autoCompleteCategory)
        val categories = arrayOf("Food", "Travel", "Shopping", "Misc")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        autoCompleteCategory.setAdapter(adapter)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, day, 0, 0, 0)
                    selectedTimestamp = selectedCalendar.timeInMillis
                    etDate.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnAddExpense.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val category = autoCompleteCategory.text.toString().trim()

            if (amountText.isEmpty() || description.isEmpty() || category.isEmpty() || selectedTimestamp == 0L) {
                Toast.makeText(
                    this,
                    "Please fill all fields correctly",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            
            val expense = ExpenseEntity(
                title = description,
                timestamp = selectedTimestamp,
                amount = amountText.toDouble(),
                category = category,
                userId = currentUser?.uid, // Save UID for logged-in user, null for guest
                isSynced = false // Start as unsynced
            )

            val db = AppDatabase.getDatabase(this)

            CoroutineScope(Dispatchers.IO).launch {
                db.expenseDao().insertExpense(expense)

                runOnUiThread {
                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Expense Saved Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Trigger sync if user is logged in
                    if (currentUser != null) {
                        scheduleSync()
                    }
                    
                    setResult(RESULT_OK)
                    finish()
                }
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
}
