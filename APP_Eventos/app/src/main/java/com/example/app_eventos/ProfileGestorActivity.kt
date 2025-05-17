package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ProfileGestorActivity : AppCompatActivity() {
    private lateinit var userId: String
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editProfileButton: Button
    private lateinit var saveButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText

    private lateinit var otherButtons: List<Button>
    private var originalName: String = ""
    private var originalEmail: String = ""
    private var originalPhone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.perfil_gestor)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

        editProfileButton = findViewById(R.id.button10)
        saveButton = findViewById(R.id.button)
        nameEditText = findViewById(R.id.editTextText4)
        emailEditText = findViewById(R.id.editTextText8)
        phoneEditText = findViewById(R.id.editTextText9)

        otherButtons = listOf(
            findViewById(R.id.button17),
            findViewById(R.id.button18),
            findViewById(R.id.button19),
            findViewById(R.id.button20)
        )

        setupButtonListeners()

        if (userId != "Desconhecido") {
            fetchUserData(userId)
        } else {
            Toast.makeText(this, "Gestor não identificado.", Toast.LENGTH_SHORT).show()
        }

        toggleEditMode(false)
    }

    private fun fetchUserData(userId: String) {
        db.collection("Utilizadores").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    originalName = document.getString("nome") ?: "Sem Nome"
                    originalEmail = document.getString("email") ?: "Sem Email"
                    originalPhone = document.getString("telefone") ?: "Sem Telefone"

                    nameEditText.setText(originalName)
                    emailEditText.setText(originalEmail)
                    phoneEditText.setText(originalPhone)
                } else {
                    Toast.makeText(this, "Dados do utilizador não encontrados.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar os dados do utilizador.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupButtonListeners() {
        val buttonHome = findViewById<Button>(R.id.button17)
        buttonHome?.setOnClickListener {
            val intent = Intent(this, HomeGestorActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonVerPedidos = findViewById<Button>(R.id.button18)
        buttonVerPedidos?.setOnClickListener {
            val intent = Intent(this, ViewPedidosActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonPerfil = findViewById<Button>(R.id.button19)
        buttonPerfil?.setOnClickListener {
            Toast.makeText(this, "Já estás no Perfil", Toast.LENGTH_SHORT).show()
        }

        val buttonMais = findViewById<Button>(R.id.button20)
        buttonMais?.setOnClickListener {
            val intent = Intent(this, MoreGestorActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        editProfileButton.setOnClickListener {
            val isEditing = phoneEditText.isFocusable
            if (isEditing) {
                toggleEditMode(false)
                restoreOriginalData()
                enableOtherButtons(true)
            } else {
                toggleEditMode(true)
                enableOtherButtons(false)
            }
        }

        saveButton.setOnClickListener {
            saveUserData()
            toggleEditMode(false)
            enableOtherButtons(true)
        }
    }

    private fun toggleEditMode(enable: Boolean) {
        phoneEditText.isFocusable = enable
        phoneEditText.isFocusableInTouchMode = enable

        nameEditText.isFocusable = false
        nameEditText.isFocusableInTouchMode = false
        emailEditText.isFocusable = false
        emailEditText.isFocusableInTouchMode = false

        saveButton.visibility = if (enable) View.VISIBLE else View.GONE
        editProfileButton.text = if (enable) "Cancelar" else "Editar Perfil"
    }

    private fun restoreOriginalData() {
        nameEditText.setText(originalName)
        emailEditText.setText(originalEmail)
        phoneEditText.setText(originalPhone)
    }

    private fun enableOtherButtons(enable: Boolean) {
        for (button in otherButtons) {
            button.isEnabled = enable
        }
    }

    private fun saveUserData() {
        val updatedPhone = phoneEditText.text.toString()

        db.collection("Utilizadores").document(userId)
            .update(mapOf("telefone" to updatedPhone))
            .addOnSuccessListener {
                Toast.makeText(this, "Dados guardados com sucesso.", Toast.LENGTH_SHORT).show()
                originalPhone = updatedPhone
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao guardar os dados.", Toast.LENGTH_SHORT).show()
            }
    }
}
