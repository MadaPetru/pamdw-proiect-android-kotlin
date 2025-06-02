package ro.adi.agroadmin.data

data class Operation(
    val type: String,
    val cost: Int,
    val plant: Int,
    val date: String,
    val revenue: String,
    val currency: String
)