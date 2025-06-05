// ro.adi.agroadmin.ui.common/AppHeaderView.kt
package ro.adi.agroadmin.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener // Import AuthStateListener
import ro.adi.agroadmin.R
import ro.adi.agroadmin.ui.LoginActivity

class AppHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var userEmailTextView: TextView
    private var lockButton: Button
    private var weatherButton: Button
    private var authStateListener: AuthStateListener // Declare the listener

    init {
        LayoutInflater.from(context).inflate(R.layout.header, this, true) // Ensure this is R.layout.header as per previous suggestions

        userEmailTextView = findViewById(R.id.userEmail)
        lockButton = findViewById(R.id.lockButton)
        weatherButton = findViewById(R.id.weatherButton)

        // Set the initial icon based on the current theme when the view is initialized
        updateThemeButtonIcon()
        setupThemeToggleButton()
        setupLogoutButton()

        // Initialize and add the AuthStateListener
        authStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            userEmailTextView.text = user?.displayName ?: user?.email ?: "Not logged in"
            // Log.d("AppHeaderView", "Auth state changed. User: ${user?.email ?: "null"}") // Add for debugging
        }
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    // Remove updateUserEmail() as it's now handled by the listener.
    // If you need to manually force an update (e.g., after a local profile change),
    // you'd call FirebaseAuth.getInstance().currentUser to get the latest.
    /*
    internal fun updateUserEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        userEmailTextView.text = user?.displayName ?: user?.email ?: "Not logged in"
    }
    */

    // Important: Remove the listener when the view is detached to prevent memory leaks
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }

    private fun setupLogoutButton() {
        lockButton.setOnClickListener {
            showConfirmationDialog("Are you sure you want to log out?") {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    }

    private fun showConfirmationDialog(
        message: String,
        onConfirm: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.modal_confim, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val msg = dialogView.findViewById<TextView>(R.id.confirmMessage)
        val cancelBtn = dialogView.findViewById<Button>(R.id.btnCancel)
        val confirmBtn = dialogView.findViewById<Button>(R.id.btnConfirm)

        msg.text = message

        cancelBtn.setOnClickListener { dialog.dismiss() }

        confirmBtn.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.show()
    }

    private fun setupThemeToggleButton() {
        weatherButton.setOnClickListener {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // Currently in Dark Mode, switch to Light Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(context, "Switched to Light Theme", Toast.LENGTH_SHORT).show()
            } else {
                // Currently in Light Mode or unspecified, switch to Dark Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(context, "Switched to Dark Theme", Toast.LENGTH_SHORT).show()
            }

            // Update the icon immediately after the theme change
            updateThemeButtonIcon()

            // Recreate the activity to apply the theme change to the entire app
            if (context is Activity) {
                (context as Activity).recreate()
            }
        }
    }

    private fun updateThemeButtonIcon() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            weatherButton.text = "🌙" // Dark theme icon (Moon)
        } else {
            weatherButton.text = "☀️" // Light theme icon (Sun)
        }
    }
}