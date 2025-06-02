package ro.adi.agroadmin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Field
import ro.adi.agroadmin.data.Operation

class FieldActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OperationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AgroAdmin) // Ensure you're using AppCompat
        setContentView(R.layout.activity_field)

        val field = intent.getParcelableExtra<Field>("field")
        field?.let {
            findViewById<TextView>(R.id.tvFieldName).text = it.name
            findViewById<TextView>(R.id.tvArea).text = "Area: ${it.area}"
            findViewById<TextView>(R.id.tvDistance).text = "Distance: ${it.distance}"
            findViewById<TextView>(R.id.tvPlant).text = "Plant: ${it.plant}"
        }


        val fieldNameText: TextView = findViewById(R.id.tvFieldName)
        fieldNameText.text = "Field: B"  // Dynamically update if needed

        recyclerView = findViewById(R.id.rvOperations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val operations = listOf(
            Operation("A", 222, 2, "222", "333", "RON")
        )

        adapter = OperationAdapter(operations)
        recyclerView.adapter = adapter
    }
}
