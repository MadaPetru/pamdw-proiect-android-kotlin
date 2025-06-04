package ro.adi.agroadmin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import ro.adi.agroadmin.ui.HomeActivity
import ro.adi.agroadmin.ui.LoginActivity
import ro.adi.agroadmin.utils.theme.AgroAdminTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroAdminTheme {
                var isAuthenticated by remember { mutableStateOf(checkIfAuthenticated()) }
                if (isAuthenticated) {
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
    }

    private fun checkIfAuthenticated(): Boolean {
        return false
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }
}