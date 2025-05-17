package com.example.app_eventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventDetailsAdapter(private val eventList: List<EventData>) :
    RecyclerView.Adapter<EventDetailsAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipoTextView: TextView = itemView.findViewById(R.id.tipoTextView)
        val localTextView: TextView = itemView.findViewById(R.id.localTextView)
        val horaTextView: TextView = itemView.findViewById(R.id.horaTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_detail, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.tipoTextView.text = "Tipo: ${event.tipo}"
        holder.localTextView.text = "Local: ${event.local}"
        holder.horaTextView.text = "Hora: ${event.hora}"
    }

    override fun getItemCount(): Int = eventList.size
}
