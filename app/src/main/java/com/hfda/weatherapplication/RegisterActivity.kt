package com.hfda.weatherapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nameInput = findViewById<EditText>(R.id.etRegisterName)
        val emailInput = findViewById<EditText>(R.id.etRegisterEmail)
        val passwordInput = findViewById<EditText>(R.id.etRegisterPassword)
        val confirmPasswordInput =
            findViewById<EditText>(R.id.etConfirmPassword)

        val registerButton =
            findViewById<Button>(R.id.btnRegister)

        val progressBar =
            findViewById<ProgressBar>(R.id.registerProgressBar)

        val backToLogin =
            findViewById<TextView>(R.id.tvBackToLogin)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when {
                name.isEmpty() -> {
                    nameInput.error = "Enter your name"
                    nameInput.requestFocus()
                }

                email.isEmpty() -> {
                    emailInput.error = "Enter your email"
                    emailInput.requestFocus()
                }

                password.isEmpty() -> {
                    passwordInput.error = "Enter a password"
                    passwordInput.requestFocus()
                }

                password.length < 6 -> {
                    passwordInput.error =
                        "Password must contain at least 6 characters"
                    passwordInput.requestFocus()
                }

                password != confirmPassword -> {
                    confirmPasswordInput.error =
                        "Passwords do not match"
                    confirmPasswordInput.requestFocus()
                }

                else -> {
                    progressBar.visibility = View.VISIBLE
                    registerButton.isEnabled = false

                    Log.d(TAG, "Attempting to create user: $email")

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            progressBar.visibility = View.GONE
                            registerButton.isEnabled = true

                            if (task.isSuccessful) {
                                Log.d(TAG, "User creation successful")
                                Toast.makeText(
                                    this,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()

                                auth.signOut()

                                val intent =
                                    Intent(this, LoginActivity::class.java)

                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                                startActivity(intent)
                                finish()
                            } else {
                                val exception = task.exception
                                Log.e(TAG, "User creation failed", exception)
                                Toast.makeText(
                                    this,
                                    exception?.localizedMessage
                                        ?: "Registration failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }

        backToLogin.setOnClickListener {
            finish()
        }
    }
}