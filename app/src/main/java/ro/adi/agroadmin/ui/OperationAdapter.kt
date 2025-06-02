package ro.adi.agroadmin.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.adi.agroadmin.R
import ro.adi.agroadmin.data.Operation

class OperationAdapter(private val operations: List<Operation>) :
    RecyclerView.Adapter<OperationAdapter.OperationViewHolder>() {

    class OperationViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val type = view.findViewById<TextView>(R.id.tvType)
        val cost = view.findViewById<TextView>(R.id.tvCost)
        val plant = view.findViewById<TextView>(R.id.tvPlant)
        val date = view.findViewById<TextView>(R.id.tvDate)
        val rev = view.findViewById<TextView>(R.id.tvRev)
        val cur = view.findViewById<TextView>(R.id.tvCur)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_operation, parent, false)
        return OperationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val op = operations[position]
        holder.type.text = op.type
        holder.cost.text = op.cost.toString()
        holder.plant.text = op.plant.toString()
        holder.date.text = op.date
        holder.rev.text = op.revenue
        holder.cur.text = op.currency
    }

    override fun getItemCount() = operations.size
}
