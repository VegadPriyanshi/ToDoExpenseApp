package com.gpslocator.todoexpanseapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Expense
import com.ExpenseAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard1)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    true
                }

                R.id.nav_add -> {
                    val intent = Intent(this, AddExpenseActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_stats -> {
                    true
                }

                else -> false
            }
        }


























        /* // 1️⃣ Find RecyclerView
         val recycler = findViewById<RecyclerView>(R.id.recyclerExpenses)

         // 2️⃣ Create Dummy Data
         val expenseList = listOf(
             Expense("Suya and garri", "11:24 AM", "₹2,500", R.drawable.fastfood),
             Expense("Bolt fee", "11:24 AM", "₹5,300", R.drawable.electric_rickshaw),
             Expense("Lumber Jacket", "11:24 AM", "₹25,000", R.drawable.shopping_bag),
             Expense("Uber", "11:24 AM", "₹1,800", R.drawable.electric_rickshaw)
         )

         // 3️⃣ Set LayoutManager
         recycler.layoutManager = LinearLayoutManager(this)

         // 4️⃣ Set Adapter
         recycler.adapter = ExpenseAdapter(expenseList)*/
    }
}