package code.theducation.music.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Contributor(
    @SerializedName("name") val name: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("link") val link: String = "",
    @SerializedName("image") val image: String = ""
) : Parcelable