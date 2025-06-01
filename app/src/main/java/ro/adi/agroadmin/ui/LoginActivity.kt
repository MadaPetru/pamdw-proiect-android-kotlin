package ro.adi.agroadmin.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ro.adi.agroadmin.R
import androidx.core.graphics.toColorInt

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // TODO: Validate and log in via Firebase
            Toast.makeText(this, "Logging in as $email", Toast.LENGTH_SHORT).show()
        }

        registerLink.setOnClickListener {
            // TODO: Navigate to registration screen
            Toast.makeText(this, "Navigate to Register", Toast.LENGTH_SHORT).show()
        }
    }
}
