package ro.adi.agroadmin.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@IgnoreExtraProperties
data class Field(
    var id: String = "",
    val name: String = "",
    val area: Int = 0,
    val distance: Int = 0,
    val plant: String = ""
) : Parcelable
