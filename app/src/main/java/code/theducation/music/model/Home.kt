package code.theducation.music.model

import androidx.annotation.StringRes
import code.theducation.music.HomeSection

data class Home(
    val arrayList: List<Any>,
    @HomeSection
    val homeSection: Int,
    @StringRes
    val titleRes: Int
)