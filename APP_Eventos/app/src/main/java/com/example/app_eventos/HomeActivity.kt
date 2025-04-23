package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.home)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

        Toast.makeText(this, "Bem-vindo, $userId", Toast.LENGTH_SHORT).show()

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        val buttonCreateEvent = findViewById<Button>(R.id.button2)
        buttonCreateEvent?.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonViewEvents = findViewById<Button>(R.id.button3)
        buttonViewEvents?.setOnClickListener {
            val intent = Intent(this, ViewEventsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonMoreOptions = findViewById<Button>(R.id.button5)
        buttonMoreOptions?.setOnClickListener {
            val intent = Intent(this, MoreOptionsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonProfile = findViewById<Button>(R.id.button4)
        buttonProfile?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonHome = findViewById<Button>(R.id.button1)
        buttonHome?.setOnClickListener {
            Toast.makeText(this, "Já estás no Home", Toast.LENGTH_SHORT).show()
        }

        val buttonCreateEvent2 = findViewById<Button>(R.id.button6)
        buttonCreateEvent2?.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonViewEvents2 = findViewById<Button>(R.id.button7)
        buttonViewEvents2?.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

    }
}