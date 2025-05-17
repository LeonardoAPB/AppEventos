package com.example.app_eventos

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SelectLocalActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var editText: EditText
    private lateinit var textView: TextView
    private var selectedMarker: Marker? = null
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selecionarlocal)
        supportActionBar?.hide()
        userId = intent.getStringExtra("USER_ID") ?: "Desconhecido"

        val context = applicationContext
        Configuration.getInstance().load(context, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        mapView = findViewById(R.id.mapView)
        editText = findViewById(R.id.editTextText7)
        textView = findViewById(R.id.textView42)

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15)
        mapView.setMultiTouchControls(true)
        mapView.controller.setCenter(GeoPoint(38.7167, -9.1395))

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)

                    selectedMarker?.let { mapView.overlays.remove(it) }

                    val newMarker = Marker(mapView)
                    newMarker.position = geoPoint
                    newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(newMarker)
                    selectedMarker = newMarker
                    mapView.invalidate()

                    GetPlaceNameTask().execute(it.latitude, it.longitude)

                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)

        val spinnerDistancia = findViewById<Spinner>(R.id.spinnerDistancia)
        val distancias = arrayOf("10km", "20km", "40km", "50km", "100km")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distancias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistancia.adapter = adapter

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textView.text = s.toString()
            }
        })
        val button14 = findViewById<Button>(R.id.button14)
        button14.setOnClickListener {
            val localTexto = editText.text.toString()
            val returnIntent = Intent()
            returnIntent.putExtra("LOCAL_EVENTO", localTexto)
            setResult(RESULT_OK, returnIntent)
            finish()
        }

    }

    private inner class GetPlaceNameTask : AsyncTask<Double, Void, String>() {
        override fun doInBackground(vararg params: Double?): String {
            val latitude = params[0]
            val longitude = params[1]
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val inputStream = InputStreamReader(connection.inputStream)
            val response = inputStream.readText()

            return extractPlaceName(response)
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            if (result.isNotEmpty()) {
                editText.setText(result)
            } else {
                Toast.makeText(this@SelectLocalActivity, "Local não encontrado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractPlaceName(jsonResponse: String): String {
        val displayNameStart = jsonResponse.indexOf("\"display_name\":\"") + 16
        val displayNameEnd = jsonResponse.indexOf("\"", displayNameStart)
        return jsonResponse.substring(displayNameStart, displayNameEnd)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

}
