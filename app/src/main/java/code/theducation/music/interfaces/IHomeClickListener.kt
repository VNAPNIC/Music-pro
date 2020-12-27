package code.theducation.music.interfaces

import code.theducation.music.model.Album
import code.theducation.music.model.Artist
import code.theducation.music.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}