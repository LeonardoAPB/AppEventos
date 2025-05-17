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
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                    val role = document.getString("role") ?: "utilizador"

                                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("USER_NAME", userName)
                                        apply()
                                    }

                                    Toast.makeText(this, "Bem-vindo, $userName!", Toast.LENGTH_SHORT).show()

                                    val mfaEnabled = document.getBoolean("mfa_enabled") ?: false
                                    if (mfaEnabled) {
                                        goToMfaVerification(userId, email, userName, role, false)
                                    } else {
                                        goToMfaVerification(userId, email, userName, role, true)
                                    }
                                } else {
                                    createNewUser(userName, email)
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

    private fun createNewUser(userName: String, email: String) {
        db.collection("Utilizadores")
            .get()
            .addOnSuccessListener { usersSnapshot ->
                var userCount = 1
                val existingIds = usersSnapshot.map { it.id }

                while (existingIds.contains("user$userCount")) {
                    userCount++
                }

                val newUserId = "user$userCount"

                val newUser = hashMapOf(
                    "nome" to userName,
                    "email" to email,
                    "telefone" to "",
                    "password" to "",
                    "role" to "utilizador",
                    "mfa_enabled" to false
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

                        goToMfaVerification(newUserId, email, userName, "utilizador", true)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Falha ao adicionar utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao criar utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMfaVerification(userId: String, email: String, userName: String, role: String, isFirstTimeSetup: Boolean) {
        val intent = Intent(this, MFAActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_EMAIL", email)
        intent.putExtra("USER_NAME", userName)
        intent.putExtra("USER_ROLE", role)
        intent.putExtra("IS_FIRST_TIME_SETUP", isFirstTimeSetup)
        startActivity(intent)
        finish()
    }

    private fun goToHome(userId: String, role: String) {
        val intent = if (role == "gestor") {
            Intent(this, HomeGestorActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

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

        val textViewForgotPassword = findViewById<TextView>(R.id.textView5)
        textViewForgotPassword?.setOnClickListener {
            val intent = Intent(this, PasswordReset1Activity::class.java)
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

                        val hashedPassword = hashPassword(password)

                        if (storedEmail == email && storedPassword == hashedPassword) {
                            Toast.makeText(this, "Login bem-sucedido", Toast.LENGTH_SHORT).show()

                            val role = document.getString("role") ?: "utilizador"
                            val userName = document.getString("nome") ?: ""

                            val mfaEnabled = document.getBoolean("mfa_enabled") ?: false
                            if (mfaEnabled) {
                                goToMfaVerification(userId, email, userName, role, false)
                            } else {
                                goToMfaVerification(userId, email, userName, role, true)
                            }
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

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
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