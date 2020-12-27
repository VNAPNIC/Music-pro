package code.theducation.music.model.smartplaylist

import androidx.annotation.DrawableRes
import code.theducation.music.R
import code.theducation.music.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)