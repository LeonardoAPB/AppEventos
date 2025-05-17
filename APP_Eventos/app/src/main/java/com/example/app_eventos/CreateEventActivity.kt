package com.example.app_eventos

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class CreateEventActivity : AppCompatActivity() {
    private lateinit var userId: String
    private val db = FirebaseFirestore.getInstance()
    private lateinit var localEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.criarevento)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

        val localTexto = intent.getStringExtra("LOCAL_EVENTO")
        localEditText = findViewById(R.id.editTextTextlocal)

        localEditText = findViewById(R.id.editTextTextlocal)

        if (localTexto != null) {
            localEditText.setText(localTexto)
        }


        setupButtonListeners()
        setupDatePicker()
        setupTimePicker()
        setupTipoEventosSelection()
        setupServicoSelection()

        localEditText.setOnClickListener {
            val intent = Intent(this, SelectLocalActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivityForResult(intent, 1)
        }

    }
    private val servicosComPrecos = mutableMapOf<String, Int>()
    private var precoTotalSelecionado: Int = 0

    private fun setupButtonListeners() {
        val buttonHome = findViewById<Button>(R.id.button1)
        buttonHome?.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonCreateEvent = findViewById<Button>(R.id.button2)
        buttonCreateEvent?.setOnClickListener {
            Toast.makeText(this, "Já estás no criar Evento!", Toast.LENGTH_SHORT).show()
        }

        val buttonViewEvent = findViewById<Button>(R.id.button3)
        buttonViewEvent?.setOnClickListener {
            val intent = Intent(this, ViewEventsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonProfile = findViewById<Button>(R.id.button4)
        buttonProfile?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonMore = findViewById<Button>(R.id.button5)
        buttonMore?.setOnClickListener {
            val intent = Intent(this, MoreOptionsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonAvancar = findViewById<Button>(R.id.button11)
        buttonAvancar?.setOnClickListener {
            showSaveConfirmationDialog()
        }

    }

    private fun showSaveConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
        builder.setMessage("Pretende mesmo guardar este evento?")
        builder.setPositiveButton("Sim") { _, _ ->
            saveEventToFirestore()
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun saveEventToFirestore() {
        val dateEditText = findViewById<EditText>(R.id.editTextDate)
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val localEditText = findViewById<EditText>(R.id.editTextTextlocal)
        val tipoEventoEditText = findViewById<EditText>(R.id.editTextText6)
        val servicosEditText = findViewById<EditText>(R.id.editTextText5)
        val quantidadePessoasEditText = findViewById<EditText>(R.id.editTextNumber)

        val data = dateEditText.text.toString()
        val hora = timeEditText.text.toString()
        val localEvento = localEditText.text.toString()
        val tipoEvento = tipoEventoEditText.text.toString()
        val servicosSelecionados = servicosEditText.text.toString()
        val quantidadePessoas = quantidadePessoasEditText.text.toString()

        if (data.isEmpty() || hora.isEmpty() || localEvento.isEmpty() || tipoEvento.isEmpty() || servicosSelecionados.isEmpty() || quantidadePessoas.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val servicosList = servicosSelecionados.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (servicosList.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um serviço.", Toast.LENGTH_SHORT).show()
            return
        }

        val quantidadePessoasInt = try {
            quantidadePessoas.toInt()
        } catch (e: NumberFormatException) {
            null
        }

        if (quantidadePessoasInt == null || quantidadePessoasInt <= 0) {
            Toast.makeText(this, "A quantidade de pessoas deve ser um número válido e maior que zero.", Toast.LENGTH_SHORT).show()
            return
        }

        val evento = hashMapOf(
            "data" to data,
            "hora" to hora,
            "local_evento" to localEvento,
            "quantidade_pessoas" to quantidadePessoasInt.toString(),
            "preco_total" to precoTotalSelecionado.toString(),
            "servicos" to servicosList,
            "tipo_evento" to tipoEvento,
            "user" to userId,
            "estado" to "Em Espera"
        )

        db.collection("Eventos").get()
            .addOnSuccessListener { documents ->
                val existingIds = documents.mapNotNull { it.id.removePrefix("evento").toIntOrNull() }
                val nextId = (1..(existingIds.maxOrNull() ?: 0) + 1).first { it !in existingIds }
                val eventId = "evento$nextId"

                db.collection("Eventos").document(eventId).set(evento)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Evento criado com sucesso! ID: $eventId", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Erro ao criar evento: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao obter a lista de eventos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupDatePicker() {
        val dateEditText = findViewById<EditText>(R.id.editTextDate)

        dateEditText.setOnClickListener {
            showDatePicker(dateEditText)
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val minDate = calendar.timeInMillis

        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this, R.style.AppTheme_DateTimePicker),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "PT"))
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    private fun setupTimePicker() {
        val timeEditText = findViewById<EditText>(R.id.editTextTime)

        timeEditText.setOnClickListener {
            showTimePicker(timeEditText)
        }
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        val timePickerDialog = TimePickerDialog(
            ContextThemeWrapper(this, R.style.AppTheme_DateTimePicker),
            { _, hourOfDay, minute ->
                val roundedMinute = if (minute < 30) 0 else 30
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, roundedMinute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.timeZone = TimeZone.getTimeZone("UTC")
                editText.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun setupTipoEventosSelection() {
        val tipoEventosEditText = findViewById<EditText>(R.id.editTextText6)
        tipoEventosEditText.setOnClickListener {
            fetchTipoEventos { listaTipoEventos ->
                showSingleSelectDialog(listaTipoEventos, tipoEventosEditText)
            }
        }
    }

    private fun fetchTipoEventos(callback: (List<String>) -> Unit) {
        db.collection("Tipo_Eventos")
            .get()
            .addOnSuccessListener { documents ->
                val tipoEventos = mutableListOf<String>()
                for (document in documents) {
                    val nome = document.getString("nome")
                    if (nome != null) {
                        tipoEventos.add(nome)
                    }
                }
                callback(tipoEventos)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao obter tipos de eventos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSingleSelectDialog(listaTipoEventos: List<String>, editText: EditText) {
        val selectedItem = intArrayOf(-1)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar Tipo de Evento")
        builder.setSingleChoiceItems(listaTipoEventos.toTypedArray(), -1) { _, which ->
            selectedItem[0] = which
        }

        builder.setNeutralButton("Novo Tipo") { _, _ ->
            showInputDialog(editText)
        }

        builder.setPositiveButton("Confirmar") { _, _ ->
            if (selectedItem[0] != -1) {
                editText.setText(listaTipoEventos[selectedItem[0]])
            } else {
                Toast.makeText(this, "Nenhum tipo de evento selecionado.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun showInputDialog(editText: EditText) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Novo Tipo de Evento")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val novoTipo = input.text.toString()
            editText.setText(novoTipo)
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun setupServicoSelection() {
        val servicosEditText = findViewById<EditText>(R.id.editTextText5)
        servicosEditText.setOnClickListener {
            fetchServicos { listaServicos ->
                showMultiSelectDialog(listaServicos, servicosEditText)
            }
        }
    }

    private fun fetchServicos(callback: (List<String>) -> Unit) {
        db.collection("Servicos")
            .get()
            .addOnSuccessListener { documents ->
                val servicos = mutableListOf<String>()
                servicosComPrecos.clear()

                for (document in documents) {
                    val nome = document.getString("nome")
                    val precoStr = document.getString("preco")
                    val preco = precoStr?.toIntOrNull() ?: 0

                    if (nome != null) {
                        servicos.add(nome)
                        servicosComPrecos[nome] = preco
                    }
                }
                callback(servicos)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao obter serviços: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMultiSelectDialog(listaServicos: List<String>, editText: EditText) {
        val selectedItems = BooleanArray(listaServicos.size)
        val selectedServicos = mutableListOf<String>()
        var totalPreco = 0

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar Serviços")

        builder.setMultiChoiceItems(listaServicos.map { it }.toTypedArray(), selectedItems) { dialog, which, isChecked ->
            val servicoSelecionado = listaServicos[which]
            val precoServico = servicosComPrecos[servicoSelecionado] ?: 0

            if (isChecked) {
                selectedServicos.add(servicoSelecionado)
                totalPreco += precoServico
            } else {
                selectedServicos.remove(servicoSelecionado)
                totalPreco -= precoServico
            }
        }

        builder.setPositiveButton("Confirmar") { _, _ ->
            editText.setText(selectedServicos.joinToString(", "))
            val precoEditText = findViewById<EditText>(R.id.editTextNumber1)
            precoEditText.setText("€ ${totalPreco}")
            precoTotalSelecionado = totalPreco
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val localEvento = data?.getStringExtra("LOCAL_EVENTO")
            if (localEvento != null) {
                localEditText.setText(localEvento)
            }
        }
    }

}


