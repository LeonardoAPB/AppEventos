package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.registese)
        db = FirebaseFirestore.getInstance()

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton?.setOnClickListener {
            registerUser()
        }
        setupPasswordVisibilityToggle()

        val cancelText = findViewById<TextView>(R.id.loginLink)
        cancelText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val name = findViewById<EditText>(R.id.nameEditText).text.toString().trim()
        val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
        val phone = findViewById<EditText>(R.id.phoneEditText).text.toString().trim()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText).text.toString().trim()
        val termsAccepted = findViewById<CheckBox>(R.id.termsCheckBox).isChecked

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Número de telefone inválido. Deve conter 9 dígitos numéricos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email inválido. Por favor insira um email válido.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "A palavra-passe deve ter pelo menos 8 caracteres, incluir uma letra maiúscula e um número.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!termsAccepted) {
            Toast.makeText(this, "Você precisa aceitar os termos e condições.", Toast.LENGTH_SHORT).show()
            return
        }

        val hashedPassword = hashPassword(password)

        db.collection("Utilizadores")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    db.collection("Utilizadores")
                        .get()
                        .addOnSuccessListener { usersSnapshot ->
                            val existingNumbers = mutableSetOf<Int>()

                            for (document in usersSnapshot.documents) {
                                val docId = document.id
                                if (docId.startsWith("user")) {
                                    val numberPart = docId.removePrefix("user").toIntOrNull()
                                    if (numberPart != null) {
                                        existingNumbers.add(numberPart)
                                    }
                                }
                            }

                            var newUserNumber = 1
                            while (existingNumbers.contains(newUserNumber)) {
                                newUserNumber++
                            }

                            val userId = "user$newUserNumber"

                            val user = hashMapOf(
                                "nome" to name,
                                "email" to email,
                                "telefone" to phone,
                                "password" to hashedPassword,
                                "role" to "utilizador"
                            )

                            db.collection("Utilizadores")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registo bem-sucedido!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Falha no registo: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                } else {
                    Toast.makeText(this, "O email já está registado. Por favor, utilize outro email.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao verificar o email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun setupPasswordVisibilityToggle() {
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val eyeImageView1 = findViewById<ImageView>(R.id.eyeImageView1)
        val eyeImageView2 = findViewById<ImageView>(R.id.eyeImageView2)

        eyeImageView1.setOnClickListener {
            togglePasswordVisibility(passwordEditText, eyeImageView1)
        }

        eyeImageView2.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, eyeImageView2)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, eyeImageView: ImageView) {
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            eyeImageView.setImageResource(R.drawable.ic_eye_on)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            eyeImageView.setImageResource(R.drawable.ic_eye_off)
        }
        editText.setSelection(editText.text.length)
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^\\d{9}$"))
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return password.matches(passwordPattern)
    }
}
