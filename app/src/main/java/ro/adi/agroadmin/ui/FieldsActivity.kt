package ro.adi.agroadmin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Field

class FieldsActivity : AppCompatActivity() {

    private lateinit var fieldList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fields)

        fieldList = findViewById(R.id.fieldList)

        val fields = listOf(
            Field("B", 9, 9, "Vv"),
            Field("Test", 1, 2, "Porumb"),
            Field("2", 2, 2, "2")
        )

        fields.forEach { field ->
            addFieldCard(field)
        }

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

        val seeFieldButton = card.findViewById<Button>(R.id.seeFieldButton)
        seeFieldButton.setOnClickListener {
            val intent = Intent(this, FieldActivity::class.java)
            intent.putExtra("field", field)
            startActivity(intent)
        }

        fieldList.addView(card)
    }

}

