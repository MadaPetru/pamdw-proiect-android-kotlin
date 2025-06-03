package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
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
}
