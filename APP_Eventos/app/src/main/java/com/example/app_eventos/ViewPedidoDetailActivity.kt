package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ViewPedidoDetailActivity : AppCompatActivity() {

    private lateinit var tipoEventoEditText: EditText
    private lateinit var localEventoEditText: EditText
    private lateinit var telefoneEditText: EditText
    private lateinit var dataEditText: EditText
    private lateinit var horaEditText: EditText
    private lateinit var quantidadeEditText: EditText
    private lateinit var servicosEditText: EditText
    private lateinit var nomeEditText: EditText
    private lateinit var estadoEditText: EditText
    private lateinit var precoEditText2: EditText
    private lateinit var preco2EditText: EditText
    private lateinit var preco2EditText4: EditText
    private lateinit var voltarButton: Button
    private lateinit var mudarEstadoButton: Button

    private var userId: String? = null
    private var eventoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedido)
        supportActionBar?.hide()

        tipoEventoEditText = findViewById(R.id.tipoEventoEditText)
        localEventoEditText = findViewById(R.id.LocalEventoEditText)
        telefoneEditText = findViewById(R.id.TelefoneEditText)
        dataEditText = findViewById(R.id.dataEditText)
        horaEditText = findViewById(R.id.horaEditText)
        quantidadeEditText = findViewById(R.id.QuantidadeEditText)
        servicosEditText = findViewById(R.id.servicosEditText)
        nomeEditText = findViewById(R.id.nomeEditText2)
        estadoEditText = findViewById(R.id.EstadoEditText)
        precoEditText2 = findViewById(R.id.precoEditText2)
        preco2EditText = findViewById(R.id.preco2EditText)
        preco2EditText4 = findViewById(R.id.preco2EditText4)
        voltarButton = findViewById(R.id.button16)
        mudarEstadoButton = findViewById(R.id.buttonMudarEstado)

        eventoId = intent.getStringExtra("evento_id")
        userId = intent.getStringExtra("user_id")

        if (eventoId != null && userId != null) {
            getEventAndUserDetails(eventoId!!, userId!!)
        } else {
            Toast.makeText(this, "Dados do evento ou utilizador em falta", Toast.LENGTH_SHORT).show()
            finish()
        }

        voltarButton.setOnClickListener {
            val intent = Intent(this, ViewPedidosActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }

        mudarEstadoButton.setOnClickListener {
            mostrarDialogoEstados()
        }


        val buttonPrecoLocal: Button = findViewById(R.id.buttonPrecoLocal)

        buttonPrecoLocal.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Inserir Preço de Local")

            val input = EditText(this)
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val valorInserido = input.text.toString().toDoubleOrNull()

                if (valorInserido != null) {
                    val valorFormatado = "€ %.2f".format(valorInserido)
                    preco2EditText.setText(valorFormatado)

                    if (eventoId != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("Eventos")
                            .document(eventoId!!)
                            .update("valor_local", valorInserido.toString())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Valor do local atualizado", Toast.LENGTH_SHORT).show()

                                val precoTotal = precoEditText2.text.toString().replace("€", "").trim().toDoubleOrNull() ?: 0.0
                                val somaTotal = precoTotal + valorInserido
                                preco2EditText4.setText("€ %.2f".format(somaTotal))
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao guardar valor: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

    }

    private fun getEventAndUserDetails(eventoId: String, userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Eventos")
            .document(eventoId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    tipoEventoEditText.setText(document.getString("tipo_evento"))
                    localEventoEditText.setText(document.getString("local_evento"))
                    telefoneEditText.setText(document.getString("telefone"))
                    dataEditText.setText(document.getString("data"))
                    horaEditText.setText(document.getString("hora"))
                    quantidadeEditText.setText(document.getString("quantidade_pessoas"))
                    estadoEditText.setText(document.getString("estado"))

                    val servicos = document.get("servicos") as? List<*>
                    servicosEditText.setText(servicos?.joinToString(", ") ?: "")

                    val precoTotalString = document.getString("preco_total")
                    val precoTotal = precoTotalString?.toDoubleOrNull() ?: 0.0
                    precoEditText2.setText("€ %.2f".format(precoTotal))

                    val valorLocalString = document.getString("valor_local")
                    val valorLocal = valorLocalString?.toDoubleOrNull() ?: 0.0
                    preco2EditText.setText("€ %.2f".format(valorLocal))

                    val somaTotal = precoTotal + valorLocal
                    preco2EditText4.setText("€ %.2f".format(somaTotal))

                    val utilizadorDoPedidoId = document.getString("user")
                    if (utilizadorDoPedidoId != null) {
                        getUserDetails(utilizadorDoPedidoId)
                    } else {
                        Toast.makeText(this, "Utilizador do pedido não encontrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Evento não encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewPedidoDetail", "Erro ao obter dados do evento", e)
                Toast.makeText(this, "Erro ao obter evento", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUserDetails(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Utilizadores")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nomeEditText.setText(document.getString("nome"))
                    telefoneEditText.setText(document.getString("telefone"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewPedidoDetail", "Erro ao obter dados do utilizador", e)
                Toast.makeText(this, "Erro ao obter utilizador", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEstados() {
        val estados = arrayOf(
            "Em Espera",
            "Confirmado não Pago",
            "Confirmado Pago",
            "Cancelado",
            "Realizado"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar novo estado")
        builder.setItems(estados) { _, which ->
            val novoEstado = estados[which]
            atualizarEstado(novoEstado)
        }
        builder.show()
    }

    private fun atualizarEstado(novoEstado: String) {
        val db = FirebaseFirestore.getInstance()

        if (eventoId == null) return

        db.collection("Eventos")
            .document(eventoId!!)
            .update("estado", novoEstado)
            .addOnSuccessListener {
                estadoEditText.setText(novoEstado)
                Toast.makeText(this, "Estado atualizado para \"$novoEstado\"", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar estado: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
