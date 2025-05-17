package com.example.app_eventos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ViewEventsActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventList: ArrayList<Event>
    private lateinit var allEvents: ArrayList<Event>

    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private lateinit var spinnerTipo: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var buttonFiltrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.verevento)

        userId = intent.getStringExtra("USER_ID") ?: ""
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventList = ArrayList()
        allEvents = ArrayList()

        editTextStartDate = findViewById(R.id.editTextStartDate)
        editTextEndDate = findViewById(R.id.editTextEndDate)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        spinnerEstado = findViewById(R.id.spinnerEstado)
        buttonFiltrar = findViewById(R.id.buttonFiltrar)

        editTextStartDate.setOnClickListener { showDatePicker(editTextStartDate) }
        editTextEndDate.setOnClickListener { showDatePicker(editTextEndDate) }
        buttonFiltrar.setOnClickListener { applyFilters() }

        loadEvents()
        setupButtonListeners()
    }

    private fun showDatePicker(target: EditText) {
        val c = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            target.setText(date)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun loadEvents() {
        db.collection("Eventos")
            .whereEqualTo("user", userId)
            .get()
            .addOnSuccessListener { documents ->
                eventList.clear()
                allEvents.clear()
                val tipos = mutableSetOf<String>()
                val estados = mutableSetOf<String>()

                for (document in documents) {
                    val evento = document.toObject(Event::class.java)
                    evento.id = document.id
                    eventList.add(evento)
                    allEvents.add(evento)
                    tipos.add(evento.tipo_evento)
                    estados.add(evento.estado)
                }

                spinnerTipo.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOf("Todos") + tipos.sorted()
                )

                spinnerEstado.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOf("Todos") + estados.sorted()
                )

                recyclerView.adapter = EventAdapter(this, eventList) { event ->
                    val intent = Intent(this, ViewEventDetailActivity::class.java)
                    intent.putExtra("evento_id", event.id)
                    intent.putExtra("user_id", event.user)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Log.w("ViewEventsActivity", "Erro ao carregar eventos.", e)
            }
    }

    private fun applyFilters() {
        val tipoSelecionado = spinnerTipo.selectedItem.toString()
        val estadoSelecionado = spinnerEstado.selectedItem.toString()
        val dataInicioStr = editTextStartDate.text.toString()
        val dataFimStr = editTextEndDate.text.toString()

        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("pt", "PT"))

        val dataInicio = if (dataInicioStr.isNotEmpty()) {
            try {
                LocalDate.parse(dataInicioStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) {
                null
            }
        } else null

        val dataFim = if (dataFimStr.isNotEmpty()) {
            try {
                LocalDate.parse(dataFimStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) {
                null
            }
        } else null

        val eventosFiltrados = allEvents.filter { evento ->
            val tipoMatch = tipoSelecionado == "Todos" || evento.tipo_evento == tipoSelecionado
            val estadoMatch = estadoSelecionado == "Todos" || evento.estado == estadoSelecionado

            val dataEvento = try {
                LocalDate.parse(evento.data, formatter)
            } catch (e: Exception) {
                null
            }

            val dataMatch = when {
                dataEvento == null -> false
                dataInicio != null && dataEvento.isBefore(dataInicio) -> false
                dataFim != null && dataEvento.isAfter(dataFim) -> false
                else -> true
            }

            tipoMatch && estadoMatch && dataMatch
        }

        eventList.clear()
        eventList.addAll(eventosFiltrados)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.button1)?.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).putExtra("USER_ID", userId))
        }
        findViewById<Button>(R.id.button2)?.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java).putExtra("USER_ID", userId))
        }
        findViewById<Button>(R.id.button3)?.setOnClickListener {
            Toast.makeText(this, "Já estás a ver eventos!", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.button4)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).putExtra("USER_ID", userId))
        }
        findViewById<Button>(R.id.button5)?.setOnClickListener {
            startActivity(Intent(this, MoreOptionsActivity::class.java).putExtra("USER_ID", userId))
        }
    }
}