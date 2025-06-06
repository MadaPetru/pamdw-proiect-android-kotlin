package ro.adi.agroadmin.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Operation

class OperationAdapter(
    private val operations: List<Operation>,
    private val onEdit: (Operation) -> Unit,
    private val onDelete: (Operation) -> Unit
) : RecyclerView.Adapter<OperationAdapter.OperationViewHolder>() {

    inner class OperationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvCost: TextView = view.findViewById(R.id.tvCost)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvRev: TextView = view.findViewById(R.id.tvRev)
        val tvCur: TextView = view.findViewById(R.id.tvCur)
        val btnEdit: Button = view.findViewById(R.id.editButton)
        val btnDelete: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation, parent, false) // Your layout file
        return OperationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val operation = operations[position]
        holder.tvType.text = operation.type
        holder.tvCost.text = operation.cost.toString()
        holder.tvDate.text = operation.date
        holder.tvRev.text = operation.revenue.toString()
        holder.tvCur.text = operation.currency

        holder.btnEdit.setOnClickListener {
            onEdit(operation)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(operation)
        }
    }

    override fun getItemCount() = operations.size
}

