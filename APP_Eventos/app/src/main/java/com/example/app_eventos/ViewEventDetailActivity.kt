package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ViewEventDetailActivity : AppCompatActivity() {

    private lateinit var tipoEventoEditText: EditText
    private lateinit var localEventoEditText: EditText
    private lateinit var telefoneEditText: EditText
    private lateinit var dataEditText: EditText
    private lateinit var horaEditText: EditText
    private lateinit var quantidadeEditText: EditText
    private lateinit var servicosEditText: EditText
    private lateinit var nomeEditText: EditText
    private lateinit var voltarButton: Button
    private lateinit var eliminarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.veroevento)
        supportActionBar?.hide()

        tipoEventoEditText = findViewById(R.id.tipoEventoEditText)
        localEventoEditText = findViewById(R.id.LocalEventoEditText)
        telefoneEditText = findViewById(R.id.TelefoneEditText)
        dataEditText = findViewById(R.id.dataEditText)
        horaEditText = findViewById(R.id.horaEditText)
        quantidadeEditText = findViewById(R.id.QuantidadeEditText)
        servicosEditText = findViewById(R.id.servicosEditText)
        nomeEditText = findViewById(R.id.NomeEditText)


        val eventoId = intent.getStringExtra("evento_id")
        val userId = intent.getStringExtra("user_id")

        if (eventoId != null && userId != null) {
            getEventoAndUserDetails(eventoId, userId)
        }

        voltarButton = findViewById(R.id.button16)
        eliminarButton = findViewById(R.id.buttonEliminar)
        voltarButton.setOnClickListener {
            finish()
        }

        eliminarButton.setOnClickListener {
            val eventoId = intent.getStringExtra("evento_id")
            if (eventoId != null) {
                confirmarEliminarEvento(eventoId)
            }
        }

    }

    private fun getEventoAndUserDetails(eventoId: String, userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Eventos")
            .document(eventoId)
            .get()
            .addOnSuccessListener { eventoSnapshot ->
                if (eventoSnapshot.exists()) {
                    val tipoEvento = eventoSnapshot.getString("tipo_evento") ?: "Desconhecido"
                    val localEvento = eventoSnapshot.getString("local_evento") ?: "Desconhecido"
                    val telefone = eventoSnapshot.getString("telefone") ?: "Desconhecido"
                    val data = eventoSnapshot.getString("data") ?: "Desconhecida"
                    val hora = eventoSnapshot.getString("hora") ?: "Desconhecida"
                    val quantidadePessoas = eventoSnapshot.getString("quantidade_pessoas") ?: "0"
                    val servicos = eventoSnapshot.get("servicos") as? List<String> ?: listOf("Sem serviços")

                    tipoEventoEditText.setText(tipoEvento)
                    localEventoEditText.setText(localEvento)
                    telefoneEditText.setText(telefone)
                    dataEditText.setText(data)
                    horaEditText.setText(hora)
                    quantidadeEditText.setText(quantidadePessoas)
                    servicosEditText.setText(servicos.joinToString(", "))

                    getUserDetails(userId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewEventDetailActivity", "Erro ao recuperar dados do evento", e)
            }
    }

    private fun getUserDetails(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Utilizadores")
            .document(userId)
            .get()
            .addOnSuccessListener { userSnapshot ->
                if (userSnapshot.exists()) {
                    val nome = userSnapshot.getString("nome") ?: "Desconhecido"
                    val telefone = userSnapshot.getString("telefone") ?: "Desconhecido"

                    nomeEditText.setText(nome)
                    telefoneEditText.setText(telefone)

                    setFieldsNonEditable()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewEventDetailActivity", "Erro ao recuperar dados do utilizador", e)
            }
    }

    private fun setFieldsNonEditable() {
        tipoEventoEditText.isFocusable = false
        tipoEventoEditText.isClickable = false
        tipoEventoEditText.isCursorVisible = false

        localEventoEditText.isFocusable = false
        localEventoEditText.isClickable = false
        localEventoEditText.isCursorVisible = false

        telefoneEditText.isFocusable = false
        telefoneEditText.isClickable = false
        telefoneEditText.isCursorVisible = false

        dataEditText.isFocusable = false
        dataEditText.isClickable = false
        dataEditText.isCursorVisible = false

        horaEditText.isFocusable = false
        horaEditText.isClickable = false
        horaEditText.isCursorVisible = false

        quantidadeEditText.isFocusable = false
        quantidadeEditText.isClickable = false
        quantidadeEditText.isCursorVisible = false

        servicosEditText.isFocusable = false
        servicosEditText.isClickable = false
        servicosEditText.isCursorVisible = false

        nomeEditText.isFocusable = false
        nomeEditText.isClickable = false
        nomeEditText.isCursorVisible = false
    }
    private fun confirmarEliminarEvento(eventoId: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
        builder.setMessage("Quer mesmo apagar este evento?")
        builder.setPositiveButton("Sim") { _, _ ->
            eliminarEvento(eventoId)
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun eliminarEvento(eventoId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Eventos")
            .document(eventoId)
            .delete()
            .addOnSuccessListener {
                Log.d("ViewEventDetailActivity", "Evento eliminado com sucesso.")
                Toast.makeText(this, "Evento eliminado com sucesso!", Toast.LENGTH_SHORT).show()

                val userId = intent.getStringExtra("user_id")
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USER_ID", userId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("ViewEventDetailActivity", "Erro ao eliminar evento.", e)
                Toast.makeText(this, "Erro ao eliminar o evento: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
