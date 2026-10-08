package com.gpslocator.todoexpanseapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("232023458505-kfr4gp9on3hkd8a7cdlrd05h3fgbnnft.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogle = findViewById<Button>(R.id.btnGoogleSignIn)
        val btnSkip = findViewById<Button>(R.id.btnSkip)

        btnGoogle.setOnClickListener {
            signIn()
        }

        btnSkip.setOnClickListener {
            // Save skip state in SharedPreferences
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("is_skipped", true).apply()

            // Navigate to Dashboard
            startActivity(Intent(this, Dashboard1::class.java))
            finish()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            firebaseAuthWithGoogle(account.idToken!!)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // Clear skip state if user successfully logs in
                    val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("is_skipped", false).apply()

                    startActivity(Intent(this, Dashboard1::class.java))
                    finish()

                } else {

                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()

                }
            }
    }
}
