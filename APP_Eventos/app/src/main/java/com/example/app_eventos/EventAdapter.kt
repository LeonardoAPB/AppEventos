package com.example.app_eventos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val context: Context,
    private val eventList: ArrayList<Event>,
    private val onItemClickListener: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event)

        holder.itemView.setOnClickListener {
            onItemClickListener(event)
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTipoEvento: TextView = itemView.findViewById(R.id.textViewTipoEvento)
        private val textViewDataHora: TextView = itemView.findViewById(R.id.textViewDataHora)

        fun bind(event: Event) {
            textViewTipoEvento.text = event.tipo_evento
            textViewDataHora.text = "${event.data} - ${event.hora}"
        }
    }
}

