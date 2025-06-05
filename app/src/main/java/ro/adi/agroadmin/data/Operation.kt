package ro.adi.agroadmin.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Operation(
    val type: String,
    val cost: Int,
    val plant: String,
    val date: String,
    val revenue: Int,
    val currency: String
):Parcelable