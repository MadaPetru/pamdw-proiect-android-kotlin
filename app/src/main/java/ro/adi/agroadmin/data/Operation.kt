package ro.adi.agroadmin.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@IgnoreExtraProperties
data class Operation(
    var id: String = "",
    val type: String = "",
    val cost: Int = 0,
    val plant: String = "",
    val date: String = "",
    val revenue: Int = 0,
    val currency: String = ""
):Parcelable