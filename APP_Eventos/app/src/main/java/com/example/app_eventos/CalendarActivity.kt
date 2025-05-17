package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventDetailsRecyclerView: RecyclerView
    private lateinit var buttonBack: Button
    private lateinit var userId: String
    private val eventDays = mutableSetOf<Int>()
    private lateinit var selectedDate: String
    private var currentMonth = 0
    private var currentYear = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.calendar)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"
        recyclerView = findViewById(R.id.calendarRecyclerView)
        eventDetailsRecyclerView = findViewById(R.id.eventDetailsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 7)
        eventDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            val intent = Intent(this, MoreOptionsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        loadEvents()
    }

    private fun loadEvents() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Eventos")
            .whereEqualTo("user", userId)
            .get()
            .addOnSuccessListener { docs ->
                val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("pt", "PT"))
                for (doc in docs) {
                    val data = doc.getString("data") ?: continue
                    try {
                        val date = dateFormat.parse(data.lowercase(Locale("pt", "PT")))
                        val cal = Calendar.getInstance()
                        cal.time = date!!
                        eventDays.add(cal.get(Calendar.DAY_OF_MONTH))
                    } catch (_: Exception) {}
                }
                setupCalendar()
            }
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        val daysList = mutableListOf<Int?>()
        repeat(firstDayWeek) { daysList.add(null) }
        for (i in 1..daysInMonth) {
            daysList.add(i)
        }

        recyclerView.adapter = DayAdapter(daysList, eventDays) { selectedDay ->
            selectedDate = formatDate(selectedDay)
            showEventDetails(selectedDate)
        }
    }

    private fun formatDate(day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)
        val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("pt", "PT"))
        return dateFormat.format(calendar.time).lowercase(Locale("pt", "PT"))
    }

    private fun showEventDetails(date: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Eventos")
            .whereEqualTo("data", date)
            .whereEqualTo("user", userId)
            .get()
            .addOnSuccessListener { docs ->
                val eventList = mutableListOf<EventData>()
                for (doc in docs) {
                    val tipo = doc.getString("tipo_evento") ?: "Desconhecido"
                    val local = doc.getString("local_evento") ?: "Desconhecido"
                    val hora = doc.getString("hora") ?: "Desconhecida"
                    eventList.add(EventData(tipo, local, hora))
                }
                updateEventDetailsRecyclerView(eventList)
            }
    }

    private fun updateEventDetailsRecyclerView(eventList: List<EventData>) {
        val adapter = EventDetailsAdapter(eventList)
        eventDetailsRecyclerView.adapter = adapter
    }
}
