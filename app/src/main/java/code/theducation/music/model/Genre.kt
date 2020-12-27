package code.theducation.music.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int
) : Parcelable