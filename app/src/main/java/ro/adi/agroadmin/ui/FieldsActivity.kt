package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Field

class FieldsActivity : AppCompatActivity() {

    private lateinit var fieldList: LinearLayout

    override fun onResume() {
        super.onResume()
        loadFieldsFromFirestore()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fields)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            loadFieldsFromFirestore()
            swipeRefresh.isRefreshing = false
        }

        val auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
        val logoutButton = findViewById<Button>(R.id.lockButton) // Correct ID for the lock button
        logoutButton.setOnClickListener {
            showConfirmationDialog("Are you sure you want to log out?") {
                auth.signOut() // Sign out from Firebase
                Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

                // Redirect to your LoginActivity or Splash screen
                val intent = Intent(this, LoginActivity::class.java) // Replace LoginActivity with your actual login screen
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
                startActivity(intent)
                finish() // Finish the current activity
            }
        }


        val createButton = findViewById<Button>(R.id.btnCreateField)

        createButton.setOnClickListener {
            Log.e("FieldsActivity", "Create button clicked")
            showFieldDialog()
        }

        val user = FirebaseAuth.getInstance().currentUser
        val emailTextView = findViewById<TextView>(R.id.userEmail)

        if (user != null) {
            // Show email or displayName if available
            emailTextView.text = user.displayName ?: user.email ?: "Unknown user"
        } else {
            emailTextView.text = "Not logged in"
        }

        fieldList = findViewById(R.id.fieldList)

        loadFieldsFromFirestore()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_fields
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_fields -> true
                else -> false
            }
        }
    }

    private fun addFieldCard(field: Field) {
        val card = layoutInflater.inflate(R.layout.field_card, null)
        card.findViewById<TextView>(R.id.tvName).text = field.name
        card.findViewById<TextView>(R.id.tvArea).text = "Area: ${field.area}"
        card.findViewById<TextView>(R.id.tvDistance).text = "Distance from farm: ${field.distance}"
        card.findViewById<TextView>(R.id.tvPlant).text = "Current plant: ${field.plant}"

        val editFieldButton = card.findViewById<Button>(R.id.editFieldButton)
        editFieldButton.setOnClickListener {
            showFieldDialog(field)
        }

        val deleteFieldButton = card.findViewById<Button>(R.id.deleteFieldButton)
        deleteFieldButton.setOnClickListener {
            showConfirmationDialog("Are you sure you want to delete this field?") {
//                deleteFieldFromFirestore(field)
            }
        }

        val seeFieldButton = card.findViewById<Button>(R.id.seeFieldButton)
        seeFieldButton.setOnClickListener {
            val intent = Intent(this, FieldActivity::class.java)
            intent.putExtra("field", field)
            startActivity(intent)
        }

        fieldList.addView(card)
    }

    private fun loadFieldsFromFirestore() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(userEmail)
                .collection("fields")
                .get()
                .addOnSuccessListener { result ->
                    fieldList.removeAllViews() // Clear old views if reloading

                    Log.e("FieldsActivity", "Loading fields for user: $userEmail")
                    for (document in result) {
                        val field = document.toObject(Field::class.java)
                        addFieldCard(field)
                    }
                    Log.e("FieldsActivity", "Fields loaded successfully")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load fields: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(
        message: String,
        onConfirm: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_confim, null)
        val dialog = AlertDialog.Builder(this)
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



    private fun showFieldDialog(fieldToEdit: Field? = null) {
        Log.e("ss","ss");
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_field, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val nameEdit = dialogView.findViewById<EditText>(R.id.fieldName)
        val areaEdit = dialogView.findViewById<EditText>(R.id.area)
        val distanceEdit = dialogView.findViewById<EditText>(R.id.distance)
        val plantEdit = dialogView.findViewById<EditText>(R.id.currentPlant)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)

        // If editing, fill data
        if (fieldToEdit != null) {
            title.text = "Edit Field"
            nameEdit.setText(fieldToEdit.name)
            areaEdit.setText(fieldToEdit.area.toString())
            distanceEdit.setText(fieldToEdit.distance.toString())
            plantEdit.setText(fieldToEdit.plant)
        } else {
            title.text = "Field"
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val area = areaEdit.text.toString().toIntOrNull()
            val distance = distanceEdit.text.toString().toIntOrNull()
            val plant = plantEdit.text.toString().trim()

            if (name.isEmpty() || area == null || distance == null || plant.isEmpty()) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val field = Field(name, area, distance, plant)

            val db = FirebaseFirestore.getInstance()
            val userEmail = FirebaseAuth.getInstance().currentUser?.email

            if (userEmail != null) {
                db.collection("users")
                    .document(userEmail)
                    .collection("fields")
                    .add(field)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Field saved", Toast.LENGTH_SHORT).show()
                        addFieldCard(field) // Add to UI list
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving field: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()
    }


}

