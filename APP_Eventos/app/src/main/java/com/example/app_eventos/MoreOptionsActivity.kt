package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MoreOptionsActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.mais)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

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

        val contentCard = findViewById<androidx.cardview.widget.CardView>(R.id.contentCard)
        val cardViews = ArrayList<CardView>()

        val linearLayout = contentCard.getChildAt(0) as android.widget.LinearLayout
        for (i in 0 until linearLayout.childCount) {
            val child = linearLayout.getChildAt(i)
            if (child is CardView) {
                cardViews.add(child)
            }
        }

        if (cardViews.size > 0) {
            cardViews[0].setOnClickListener {
                val intent = Intent(this, CalendarActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }
        }

        if (cardViews.size > 1) {
            cardViews[1].setOnClickListener {
                Toast.makeText(this, "Notificações em breve!", Toast.LENGTH_SHORT).show()
            }
        }

        if (cardViews.size > 2) {
            cardViews[2].setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}