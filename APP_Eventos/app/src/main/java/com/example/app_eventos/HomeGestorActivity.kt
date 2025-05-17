package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeGestorActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.home_gestor)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        val buttonHome = findViewById<Button>(R.id.button8)
        buttonHome?.setOnClickListener {
            Toast.makeText(this, "Já estás no Home", Toast.LENGTH_SHORT).show()
        }

        val buttonVerPedidos = findViewById<Button>(R.id.button12)
        buttonVerPedidos?.setOnClickListener {
            val intent = Intent(this, ViewPedidosActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonPerfil = findViewById<Button>(R.id.button13)
        buttonPerfil?.setOnClickListener {
            val intent = Intent(this, ProfileGestorActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonMais = findViewById<Button>(R.id.button15)
        buttonMais?.setOnClickListener {
            val intent = Intent(this, MoreGestorActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonVerPedidos2 = findViewById<Button>(R.id.button6)
        buttonVerPedidos2?.setOnClickListener {
            val intent = Intent(this, ViewPedidosActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }
}
