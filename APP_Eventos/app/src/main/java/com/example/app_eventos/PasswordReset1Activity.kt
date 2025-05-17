package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class PasswordReset1Activity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var codeEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var nextButton: Button
    private var generatedCode: String? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recuperar1)
        supportActionBar?.hide()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        codeEditText = findViewById(R.id.codeEditText)
        sendCodeButton = findViewById(R.id.sendCodeButton)
        nextButton = findViewById(R.id.advanceButton)

        sendCodeButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Insere um email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("Utilizadores")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "Email não encontrado", Toast.LENGTH_SHORT).show()
                    } else {
                        generatedCode = Random.nextInt(100000, 999999).toString()
                        userEmail = email
                        sendEmail(email, generatedCode!!)
                        Toast.makeText(this, "Código enviado para o email", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        nextButton.setOnClickListener {
            val inputCode = codeEditText.text.toString().trim()
            if (inputCode == generatedCode && userEmail != null) {
                val intent = Intent(this, PasswordReset2Activity::class.java)
                intent.putExtra("email", userEmail)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Código incorreto", Toast.LENGTH_SHORT).show()
            }
        }

        val cancelText = findViewById<TextView>(R.id.cancelText)
        cancelText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun sendEmail(toEmail: String, code: String) {
        println("Código de verificação enviado para $toEmail: $code")
    }

}
