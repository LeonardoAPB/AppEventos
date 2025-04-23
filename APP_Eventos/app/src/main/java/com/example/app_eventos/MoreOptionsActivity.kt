package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MoreOptionsActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.mais)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

        Toast.makeText(this, "Mais Opções, $userId", Toast.LENGTH_SHORT).show()

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        val buttonHome = findViewById<Button>(R.id.button1)
        buttonHome?.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonCreateEvent = findViewById<Button>(R.id.button2)
        buttonCreateEvent?.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
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
            Toast.makeText(this, "Já estás no Mais Opções!", Toast.LENGTH_SHORT).show()
        }

        val view16 = findViewById<View>(R.id.view16)
        view16?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
