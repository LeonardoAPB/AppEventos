package com.example.app_eventos

import Event
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val context: Context, private val eventList: ArrayList<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewEventDetailActivity::class.java)
            intent.putExtra("evento_id", event.id)
            intent.putExtra("user_id", event.user)
            context.startActivity(intent)
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

