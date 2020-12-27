package code.theducation.music.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import code.theducation.music.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryInfo(
    val category: Category,
    @get:JvmName("isVisible")
    var visible: Boolean
) : Parcelable {

    enum class Category(
        val id: Int,
        @StringRes val stringRes: Int,
        @DrawableRes val icon: Int
    ) {
        Home(R.id.action_home, R.string.for_you, R.drawable.ic_face),
        Songs(R.id.action_song, R.string.songs, R.drawable.ic_audiotrack),
        Albums(R.id.action_album, R.string.albums, R.drawable.ic_album),
        Artists(R.id.action_artist, R.string.artists, R.drawable.ic_artist),
        Playlists(R.id.action_playlist, R.string.playlists, R.drawable.ic_queue_music),
        Genres(R.id.action_genre, R.string.genres, R.drawable.ic_guitar),
        Folder(R.id.action_folder, R.string.folders, R.drawable.ic_folder);
    }
}