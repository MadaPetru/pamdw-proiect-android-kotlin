package ro.adi.agroadmin.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OperationChartDto(
    val type: String,
    val cost: Double,
    val plant: String,
    val date: String,
    val revenue: Double,
    val fieldName: String,
    val currency: String
): Parcelable
