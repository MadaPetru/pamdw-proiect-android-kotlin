package ro.adi.agroadmin.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Field(val name: String, val area: Int, val distance: Int, val plant: String) : Parcelable
