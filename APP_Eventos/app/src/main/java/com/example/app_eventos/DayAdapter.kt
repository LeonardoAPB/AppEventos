package com.example.app_eventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(
    private val days: List<Int?>,
    private val eventDays: Set<Int>,
    private val onDayClick: (Int) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText = itemView.findViewById<TextView>(R.id.textDay)
        val indicator = itemView.findViewById<View>(R.id.eventIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        if (day != null) {
            holder.dayText.text = day.toString()
            holder.dayText.visibility = View.VISIBLE
            holder.indicator.visibility = if (eventDays.contains(day)) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener {
                onDayClick(day)
            }
        } else {
            holder.dayText.text = ""
            holder.indicator.visibility = View.GONE
        }
    }

    override fun getItemCount() = days.size
}
