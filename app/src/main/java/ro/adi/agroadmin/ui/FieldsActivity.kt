package ro.adi.agroadmin.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Field

class FieldsActivity : AppCompatActivity() {

    private lateinit var fieldList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fields)

        val createButton = findViewById<Button>(R.id.btnCreateField)

        createButton.setOnClickListener {
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

    private fun showFieldDialog(fieldToEdit: Field? = null) {
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
            val field = Field(
                name = nameEdit.text.toString(),
                area = areaEdit.text.toString().toInt(),
                distance = distanceEdit.text.toString().toInt(),
                plant = plantEdit.text.toString()
            )

            // TODO: Add field to list or update database
            Toast.makeText(this, "Saved: ${field.name}", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        dialog.show()
    }


}

