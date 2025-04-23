package com.example.app_eventos

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        setupGoogleSignIn()
        setupButtonListeners()
        setupPasswordVisibilityToggle()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleLoginButton = findViewById<ImageView>(R.id.imageView3)
        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Erro no login com Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val googleAccount = GoogleSignIn.getLastSignedInAccount(this)

                    if (googleAccount != null) {
                        val userName = googleAccount.displayName ?: "Usuário Desconhecido"
                        val email = googleAccount.email ?: "Desconhecido"

                        db.collection("Utilizadores")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val document = documents.documents.first()
                                    val userId = document.id

                                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("USER_NAME", userName)
                                        apply()
                                    }

                                    Toast.makeText(this, "Bem-vindo, $userName!", Toast.LENGTH_SHORT).show()

                                    goToHome(userId)
                                } else {
                                    db.collection("Utilizadores")
                                        .get()
                                        .addOnSuccessListener { usersSnapshot ->
                                            val userCount = usersSnapshot.size() + 1
                                            val newUserId = "user$userCount"

                                            val newUser = hashMapOf(
                                                "nome" to userName,
                                                "email" to email,
                                                "telefone" to "",
                                                "password" to ""
                                            )

                                            db.collection("Utilizadores")
                                                .document(newUserId)
                                                .set(newUser)
                                                .addOnSuccessListener {
                                                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                                    with(sharedPref.edit()) {
                                                        putString("USER_NAME", userName)
                                                        apply()
                                                    }

                                                    Toast.makeText(this, "Bem-vindo, $userName!", Toast.LENGTH_SHORT).show()

                                                    goToHome(newUserId)
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this, "Falha ao adicionar utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao verificar utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Falha na autenticação", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToHome(userId: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }

    private fun setupButtonListeners() {
        val textView9 = findViewById<TextView>(R.id.registese1)
        textView9?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val loginbutton = findViewById<Button>(R.id.loginbutton)
        loginbutton?.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.emailEditText)
            val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, insira o email e a senha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        getUserFromFirestore(email, password)
    }

    private fun getUserFromFirestore(email: String, password: String) {
        val usersRef = db.collection("Utilizadores")
        usersRef.whereEqualTo("email", email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && documents.size() > 0) {
                        val document = documents.documents.first()
                        val storedEmail = document.getString("email")
                        val storedPassword = document.getString("password")
                        val userId = document.id

                        if (storedEmail == email && storedPassword == password) {
                            Toast.makeText(this, "Login bem-sucedido", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, HomeActivity::class.java)
                            intent.putExtra("USER_ID", userId)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Utilizador não encontrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Falha ao acessar os dados da Firestore", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupPasswordVisibilityToggle() {
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val eyeImageView = findViewById<ImageView>(R.id.eyeImageView)

        eyeImageView.setOnClickListener {
            if (passwordEditText.transformationMethod == PasswordTransformationMethod.getInstance()) {
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                eyeImageView.setImageResource(R.drawable.ic_eye_on)
            } else {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeImageView.setImageResource(R.drawable.ic_eye_off)
            }

            passwordEditText.setSelection(passwordEditText.text.length)
        }
    }
}
