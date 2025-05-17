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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ViewPedidosActivity : AppCompatActivity() {
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

    private val firebaseDateFormat = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("pt", "PT"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.verpedidos)

        userId = intent.getStringExtra("USER_ID") ?: ""
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewPedidos)
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
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            target.setText(dateStr)
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun loadEvents() {
        db.collection("Eventos")
            .get()
            .addOnSuccessListener { documents ->
                eventList.clear()
                allEvents.clear()
                val tipos = mutableSetOf<String>()
                val estados = mutableSetOf<String>()

                for (document in documents) {
                    val evento = document.toObject(Event::class.java)
                    evento.id = document.id
                    allEvents.add(evento)
                    tipos.add(evento.tipo_evento)
                    estados.add(evento.estado)
                }

                val adapterTipo = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOf("Todos") + tipos.sorted()
                )
                adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTipo.adapter = adapterTipo

                val adapterEstado = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOf("Todos") + estados.sorted()
                )
                adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerEstado.adapter = adapterEstado

                eventList.clear()
                eventList.addAll(allEvents)
                recyclerView.adapter = EventAdapter(this, eventList) { event ->
                    val intent = Intent(this, ViewPedidoDetailActivity::class.java)
                    intent.putExtra("evento_id", event.id)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Log.w("VerPedidosActivity", "Erro ao carregar eventos.", e)
            }
    }

    private fun applyFilters() {
        val tipoSelecionado = spinnerTipo.selectedItem.toString()
        val estadoSelecionado = spinnerEstado.selectedItem.toString()
        val dataInicioStr = editTextStartDate.text.toString()
        val dataFimStr = editTextEndDate.text.toString()

        val dataInicio = if (dataInicioStr.isNotEmpty()) {
            try {
                LocalDate.parse(dataInicioStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        } else null

        val dataFim = if (dataFimStr.isNotEmpty()) {
            try {
                LocalDate.parse(dataFimStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        } else null

        val eventosFiltrados = allEvents.filter { evento ->
            val tipoMatch = tipoSelecionado == "Todos" || evento.tipo_evento == tipoSelecionado
            val estadoMatch = estadoSelecionado == "Todos" || evento.estado == estadoSelecionado

            val dataEvento = try {
                LocalDate.parse(evento.data, firebaseDateFormat)
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

        if (eventosFiltrados.isEmpty()) {
            Toast.makeText(this, "Nenhum evento encontrado com esses filtros.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.button1)?.setOnClickListener {
            startActivity(Intent(this, HomeGestorActivity::class.java).putExtra("USER_ID", userId))
        }
        findViewById<Button>(R.id.button3)?.setOnClickListener {
            Toast.makeText(this, "Já estás no ver pedidos!", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.button4)?.setOnClickListener {
            startActivity(Intent(this, ProfileGestorActivity::class.java).putExtra("USER_ID", userId))
        }
        findViewById<Button>(R.id.button5)?.setOnClickListener {
            startActivity(Intent(this, MoreGestorActivity::class.java).putExtra("USER_ID", userId))
        }
    }
}
