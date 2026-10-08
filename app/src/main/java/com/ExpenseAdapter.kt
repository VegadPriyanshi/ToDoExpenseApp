package com

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gpslocator.todoexpanseapp.R

class ExpenseAdapter(private val list: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tvTitle)
        val time = view.findViewById<TextView>(R.id.tvTime)
        val amount = view.findViewById<TextView>(R.id.tvAmount)
        val icon = view.findViewById<ImageView>(R.id.imgCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = list[position]
        holder.title.text = expense.title
        holder.time.text = expense.time
        holder.amount.text = expense.amount
        holder.icon.setImageResource(expense.icon)
    }
}