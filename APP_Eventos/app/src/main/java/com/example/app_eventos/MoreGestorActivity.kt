package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MoreGestorActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.mais_gestor)

        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"


        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        val buttonHome = findViewById<Button>(R.id.button21)
        buttonHome?.setOnClickListener {
            val intent = Intent(this, HomeGestorActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonVerPedidos = findViewById<Button>(R.id.button22)
        buttonVerPedidos?.setOnClickListener {
            val intent = Intent(this, ViewPedidosActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonPerfil = findViewById<Button>(R.id.button23)
        buttonPerfil?.setOnClickListener {
            val intent = Intent(this, ProfileGestorActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        val buttonMais = findViewById<Button>(R.id.button24)
        buttonMais?.setOnClickListener {
            Toast.makeText(this, "Já estás no Mais Opções!", Toast.LENGTH_SHORT).show()
        }

        val contentCard = findViewById<CardView>(R.id.contentCard)
        val cardViews = ArrayList<CardView>()

        val linearLayout = contentCard.getChildAt(0) as? android.widget.LinearLayout
        linearLayout?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChildAt(i)
                if (child is CardView) {
                    cardViews.add(child)
                }
            }
        }

        if (cardViews.size > 0) {
            cardViews[0].setOnClickListener {
                Toast.makeText(this, "Notificações em breve!", Toast.LENGTH_SHORT).show()
            }
        }

        if (cardViews.size > 1) {
            cardViews[1].setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
