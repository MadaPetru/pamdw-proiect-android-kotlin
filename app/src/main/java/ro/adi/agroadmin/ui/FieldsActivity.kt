package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable // Import Editable
import android.text.TextWatcher // Import TextWatcher
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
import ro.adi.agroadmin.ui.common.AppHeaderView // Keep this if AppHeaderView is still used in XML

class FieldsActivity : AppCompatActivity() {

    private lateinit var fieldList: LinearLayout
    private lateinit var searchField: EditText // Declare searchField here

    override fun onResume() {
        super.onResume()
        // Load fields when activity resumes, initially with no search query
        loadFieldsFromFirestore()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fields)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        searchField = findViewById(R.id.searchField) // Initialize searchField

        swipeRefresh.setOnRefreshListener {
            // When refreshing, re-run the search with current query
            loadFieldsFromFirestore(searchField.text.toString())
            swipeRefresh.isRefreshing = false
        }

        val createButton = findViewById<Button>(R.id.btnCreateField)
        createButton.setOnClickListener {
            Log.e("FieldsActivity", "Create button clicked")
            showFieldDialog()
        }

        fieldList = findViewById(R.id.fieldList)

        // Add TextWatcher to searchField
        setupSearchField() // Call the setup function

        // Initial load of fields is already in onResume(), no need to call again here
        // loadFieldsFromFirestore()

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

    // --- Search Setup Function ---
    private fun setupSearchField() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this functionality
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Trigger search every time text changes
                loadFieldsFromFirestore(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for this functionality
            }
        })
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
                deleteFieldFromFirestore(field)
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

    private fun deleteFieldFromFirestore(field: Field) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail == null) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (field.id.isEmpty()) {
            Toast.makeText(this, "Error: Cannot delete field, ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userEmail)
            .collection("fields")
            .document(field.id) // Use the field's stored ID to delete the specific document
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Field '${field.name}' deleted successfully!", Toast.LENGTH_SHORT).show()
                // After deletion, refresh the list to reflect the change
                loadFieldsFromFirestore(searchField.text.toString()) // Pass current search query
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting field: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FieldsActivity", "Error deleting field: ${e.message}", e)
            }
    }

    // --- Modified loadFieldsFromFirestore to support searching ---
    private fun loadFieldsFromFirestore(searchQuery: String = "") { // Added optional searchQuery parameter
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()
            val fieldsCollectionRef = db.collection("users")
                .document(userEmail)
                .collection("fields")

            // Query construction based on search
            val query = if (searchQuery.isNotBlank()) {
                // Case-insensitive search requires converting to lowercase (Firestore doesn't have native case-insensitivity)
                // For a true "contains" search, you'd need more advanced techniques like Algolia or client-side filtering.
                // This performs a "starts with" or exact match search on 'name' or 'plant'.
                // Firestore doesn't directly support 'contains' queries efficiently without an external search service.
                // For a simple 'starts with' filter:
                fieldsCollectionRef.whereGreaterThanOrEqualTo("name", searchQuery)
                    .whereLessThanOrEqualTo("name", searchQuery + '\uf8ff')
                // You can add more conditions if you want to search across multiple fields,
                // but beware of Firestore query limitations (e.g., cannot use range filters on multiple fields in a single query)
            } else {
                fieldsCollectionRef // No search query, get all fields
            }

            query.get()
                .addOnSuccessListener { result ->
                    fieldList.removeAllViews() // Clear old views if reloading

                    Log.e("FieldsActivity", "Loading fields for user: $userEmail with query: '$searchQuery'")
                    for (document in result) {
                        val field = document.toObject(Field::class.java)
                        // Client-side filtering for 'contains' if the Firestore query isn't enough
                        if (searchQuery.isBlank() ||
                            field.name.contains(searchQuery, ignoreCase = true)
                        ) {
                            Log.e("load fields"," Field loaded: ${field.name} with ID: ${field.id}")
                            addFieldCard(field)
                        }
                    }
                    Log.e("FieldsActivity", "Fields loaded successfully")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load fields: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("FieldsActivity", "Failed to load fields", e)
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
        Log.e("showFieldDialog", "Called with fieldToEdit: ${fieldToEdit?.id ?: "null"}")
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

        if (fieldToEdit != null) {
            title.text = "Edit Field"
            nameEdit.setText(fieldToEdit.name)
            areaEdit.setText(fieldToEdit.area.toString())
            distanceEdit.setText(fieldToEdit.distance.toString())
            plantEdit.setText(fieldToEdit.plant)
        } else {
            title.text = "Add New Field"
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

            val db = FirebaseFirestore.getInstance()
            val userEmail = FirebaseAuth.getInstance().currentUser?.email

            if (userEmail != null) {
                val userFieldsCollection = db.collection("users")
                    .document(userEmail)
                    .collection("fields")

                if (fieldToEdit == null) {
                    // This is for ADDING a new field
                    val newDocRef = userFieldsCollection.document()
                    val newFieldId = newDocRef.id

                    val newField = Field(newFieldId, name, area, distance, plant)

                    newDocRef.set(newField)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Field added successfully!", Toast.LENGTH_SHORT).show()
                            loadFieldsFromFirestore(searchField.text.toString()) // Pass current search query
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error adding field: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("showFieldDialog", "Error adding field", e)
                        }
                } else {
                    // This is for EDITING an existing field
                    val updatedField = Field(fieldToEdit.id, name, area, distance, plant)

                    userFieldsCollection.document(fieldToEdit.id)
                        .set(updatedField)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Field updated successfully!", Toast.LENGTH_SHORT).show()
                            loadFieldsFromFirestore(searchField.text.toString()) // Pass current search query
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error updating field: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("showFieldDialog", "Error updating field", e)
                        }
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}