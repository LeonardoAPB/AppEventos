package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class MFAActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var userId: String = ""
    private var userEmail: String = ""
    private var userName: String = ""
    private var userRole: String = ""
    private var verificationCode: String = ""
    private var isFirstTimeSetup: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.mfa)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mfa_verification)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("USER_ID") ?: ""
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        userName = intent.getStringExtra("USER_NAME") ?: ""
        userRole = intent.getStringExtra("USER_ROLE") ?: "utilizador"
        isFirstTimeSetup = intent.getBooleanExtra("IS_FIRST_TIME_SETUP", false)

        val textInstructions = findViewById<TextView>(R.id.textInstructions)
        val codeEditText = findViewById<EditText>(R.id.codeEditText)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val resendCodeButton = findViewById<Button>(R.id.resendCodeButton)
        val skipMfaButton = findViewById<Button>(R.id.skipMfaButton)

        checkMfaStatus()

        generateAndSendCode()

        verifyButton.setOnClickListener {
            val enteredCode = codeEditText.text.toString().trim()
            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o código de verificação", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (enteredCode == verificationCode) {
                if (isFirstTimeSetup) {
                    updateMfaPreference(true)
                }
                proceedToHome()
            } else {
                Toast.makeText(this, "Código inválido. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
        }

        resendCodeButton.setOnClickListener {
            generateAndSendCode()
            Toast.makeText(this, "Novo código enviado para $userEmail", Toast.LENGTH_SHORT).show()
        }

        skipMfaButton.setOnClickListener {
            if (isFirstTimeSetup) {
                updateMfaPreference(false)
            }
            proceedToHome()
        }
    }

    private fun checkMfaStatus() {
        val skipMfaButton = findViewById<Button>(R.id.skipMfaButton)

        if (userId.isNotEmpty()) {
            db.collection("Utilizadores").document(userId).get()
                .addOnSuccessListener { document ->
                    val mfaEnabled = document.getBoolean("mfa_enabled") ?: false

                    skipMfaButton.visibility = if (isFirstTimeSetup || !mfaEnabled) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao verificar status de MFA: ${e.message}", Toast.LENGTH_SHORT).show()
                    skipMfaButton.visibility = android.view.View.VISIBLE
                }
        } else {
            skipMfaButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun generateAndSendCode() {
        val random = Random()
        verificationCode = String.format("%06d", random.nextInt(1000000))

        Log.d("MFAActivity", "Código de verificação enviado para $userEmail: $verificationCode")

        Toast.makeText(this, "Código de verificação gerado", Toast.LENGTH_SHORT).show()
    }

    private fun updateMfaPreference(enabled: Boolean) {
        if (userId.isNotEmpty()) {
            db.collection("Utilizadores").document(userId)
                .update("mfa_enabled", enabled)
                .addOnSuccessListener {
                    val message = if (enabled) {
                        "Verificação em duas etapas ativada."
                    } else {
                        "Verificação em duas etapas desativada."
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar preferências: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun proceedToHome() {
        val intent = if (userRole == "gestor") {
            Intent(this, HomeGestorActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_NAME", userName)
        startActivity(intent)
        finish()
    }
}