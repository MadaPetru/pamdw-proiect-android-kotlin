package ro.adi.agroadmin.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Operation

class OperationAdapter(private var operations: List<Operation>) :
    RecyclerView.Adapter<OperationAdapter.OperationViewHolder>() {

    class OperationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val type = view.findViewById<TextView>(R.id.tvType)
        val cost = view.findViewById<TextView>(R.id.tvCost)
        val date = view.findViewById<TextView>(R.id.tvDate)
        val rev = view.findViewById<TextView>(R.id.tvRev)
        val cur = view.findViewById<TextView>(R.id.tvCur)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation, parent, false) // Assuming you have item_operation.xml
        return OperationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val op = operations[position]
        holder.type.text = op.type
        holder.cost.text = op.cost.toString()
        holder.date.text = op.date
        holder.rev.text = op.revenue.toString()
        holder.cur.text = op.currency
    }

    override fun getItemCount() = operations.size
}
