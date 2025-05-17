package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class PasswordReset2Activity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var db: FirebaseFirestore
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recuperar2)
        supportActionBar?.hide()

        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        confirmButton = findViewById(R.id.confirmButton)
        db = FirebaseFirestore.getInstance()

        userEmail = intent.getStringExtra("email")

        confirmButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preenche todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(newPassword)) {
                Toast.makeText(this, "A password deve ter pelo menos 8 caracteres, incluir uma letra maiúscula e um número.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "As passwords não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userEmail == null) {
                Toast.makeText(this, "Erro ao obter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = hashPassword(newPassword)

            db.collection("Utilizadores")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first()
                        db.collection("Utilizadores")
                            .document(userDoc.id)
                            .update("password", hashedPassword)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Password atualizada com sucesso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao atualizar password", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Utilizador não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        val cancelText = findViewById<TextView>(R.id.cancelText)
        cancelText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return password.matches(passwordPattern)
    }
}
