package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
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

        val operations = listOf(
            Operation("A", 222, 2, "222", "333", "RON")
        )

        adapter = OperationAdapter(operations)
        recyclerView.adapter = adapter

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

        // Currency options
        val currencies = arrayOf("RON", "EUR", "USD")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
        currencySpinner.adapter = adapter

        // Populate if editing
        if (operation != null) {
            type.setText(operation.type)
            cost.setText(operation.cost.toString())
            plant.setText(operation.plant)
            date.setText(operation.date)
            revenue.setText(operation.revenue.toString())
            val position = currencies.indexOf(operation.currency)
            if (position >= 0) currencySpinner.setSelection(position)
        }

        cancel.setOnClickListener { dialog.dismiss() }

        save.setOnClickListener {
            val newOperation = Operation(
                type = type.text.toString(),
                cost = cost.text.toString().toDoubleOrNull() ?: 0.0,
                plant = plant.text.toString(),
                date = date.text.toString(),
                revenue = revenue.text.toString().toDoubleOrNull() ?: 0.0,
                currency = currencySpinner.selectedItem.toString()
            )
            // TODO: handle save
            Toast.makeText(this, "Saved ${newOperation.type}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    data class Operation(
        val type: String,
        val cost: Double,
        val plant: String,
        val date: String,
        val revenue: Double,
        val currency: String
    )

}
