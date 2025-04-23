package com.example.app_eventos

import Event
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ViewEventsActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventList: ArrayList<Event>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.verevento)

        userId = intent.getStringExtra("USER_ID") ?: ""
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventList = ArrayList()

        loadEvents()

        setupButtonListeners()
    }

    private fun loadEvents() {
        db.collection("Eventos")
            .whereEqualTo("user", userId)
            .get()
            .addOnSuccessListener { documents ->
                eventList.clear()
                for (document in documents) {
                    val evento = document.toObject(Event::class.java)
                    evento.id = document.id
                    eventList.add(evento)
                }
                recyclerView.adapter = EventAdapter(this, eventList)
            }
            .addOnFailureListener { exception ->
                Log.w("ViewEventsActivity", "Erro ao carregar eventos.", exception)
            }
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
            Toast.makeText(this, "Já estás a ver eventos!", Toast.LENGTH_SHORT).show()
        }

        val buttonProfile = findViewById<Button>(R.id.button4)
        buttonProfile?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonMore = findViewById<Button>(R.id.button5)
        buttonMore?.setOnClickListener {
            val intent = Intent(this, MoreOptionsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

    }

}





