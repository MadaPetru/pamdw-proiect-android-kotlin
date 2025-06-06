package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Field
import ro.adi.agroadmin.data.Operation

class FieldActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OperationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AgroAdmin)
        setContentView(R.layout.activity_field)

        val createButton = findViewById<Button>(R.id.btnCreateOperation)

        val searchOperation = findViewById<EditText>(R.id.searchOperation)

        searchOperation.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchOperations(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })


        createButton.setOnClickListener {
            showOperationDialog()
        }

        val field = intent.getParcelableExtra<Field>("field")
        if (field == null) {
            // Handle the error, e.g., finish the activity or show a message
            finish()
            return
        }

        findViewById<TextView>(R.id.tvFieldName).text = "Field: ${field.name}"

        recyclerView = findViewById(R.id.rvOperations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchOperations("")
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_fields -> {
                    startActivity(Intent(this, FieldsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchOperations(queryText: String) {
        val field = intent.getParcelableExtra<Field>("field")
        if (field == null) {
            Toast.makeText(this, "Field not found", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("fields")
            .document(field.id)
            .collection("operations")
            .get()
            .addOnSuccessListener { result ->
                val filteredOperations = result.documents.mapNotNull { doc ->
                    val op = doc.toObject(Operation::class.java)
                    op?.apply { id = doc.id }
                }.filter {
                        it.type.contains(queryText, ignoreCase = true) ||
                                it.plant.contains(queryText, ignoreCase = true)
                    }

                adapter = OperationAdapter(filteredOperations,
                    onEdit = { operation -> showOperationDialog(operation) },
                    onDelete = { operation -> confirmDeleteOperation(operation) }
                )
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching operations: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun showOperationDialog(operation: Operation? = null) {
        val view = LayoutInflater.from(this).inflate(R.layout.modal_operation, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val type = view.findViewById<EditText>(R.id.operationType)
        val cost = view.findViewById<EditText>(R.id.operationCost)
        val plant = view.findViewById<EditText>(R.id.operationPlant)
        val date = view.findViewById<EditText>(R.id.operationDate)
        val revenue = view.findViewById<EditText>(R.id.operationRevenue)
        val currencySpinner = view.findViewById<Spinner>(R.id.operationCurrency)

        val cancel = view.findViewById<Button>(R.id.cancelBtn)
        val save = view.findViewById<Button>(R.id.saveBtn)

        // Currency spinner setup
        val currencies = arrayOf("RON", "EUR", "USD")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
        currencySpinner.adapter = spinnerAdapter

        // Pre-fill if editing
        if (operation != null) {
            type.setText(operation.type)
            cost.setText(operation.cost.toString())
            plant.setText(operation.plant)
            date.setText(operation.date)
            revenue.setText(operation.revenue.toString())
            val pos = currencies.indexOf(operation.currency)
            if (pos >= 0) currencySpinner.setSelection(pos)
        }

        val field = intent.getParcelableExtra<Field>("field")
        if (field == null) {
            Toast.makeText(this, "Field not found", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        cancel.setOnClickListener { dialog.dismiss() }

        save.setOnClickListener {
            val updatedOperation = Operation(
                type = type.text.toString(),
                cost = cost.text.toString().toIntOrNull() ?: 0,
                plant = plant.text.toString(),
                date = date.text.toString(),
                revenue = revenue.text.toString().toIntOrNull() ?: 0,
                currency = currencySpinner.selectedItem.toString(),
                id = operation?.id ?: "" // retain ID for update
            )

            val db = FirebaseFirestore.getInstance()
            val opsCollection = db.collection("fields")
                .document(field.id)
                .collection("operations")

            val task = if (operation == null) {
                opsCollection.add(updatedOperation)
            } else {
                opsCollection.document(operation.id).set(updatedOperation)
            }

            task.addOnSuccessListener {
                fetchOperations("")
                Toast.makeText(this, "Operation saved", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(this, "Error saving: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun confirmDeleteOperation(operation: Operation) {
        val field = intent.getParcelableExtra<Field>("field") ?: return

        AlertDialog.Builder(this)
            .setTitle("Delete Operation")
            .setMessage("Are you sure you want to delete this operation?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance()
                    .collection("fields")
                    .document(field.id)
                    .collection("operations")
                    .document(operation.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Operation deleted", Toast.LENGTH_SHORT).show()
                        fetchOperations("")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Delete failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
