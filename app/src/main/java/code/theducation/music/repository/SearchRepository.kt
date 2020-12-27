package code.theducation.music.repository

import android.content.Context
import code.theducation.music.R
import code.theducation.music.model.Genre
import java.util.*

class RealSearchRepository(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val roomRepository: RoomRepository,
    private val genreRepository: GenreRepository,
) {
    fun searchAll(context: Context, query: String?): MutableList<Any> {
        val results = mutableListOf<Any>()
        query?.let { searchString ->
            val songs = songRepository.songs(searchString)
            if (songs.isNotEmpty()) {
                results.add(context.resources.getString(R.string.songs))
                results.addAll(songs)
            }
            val artists = artistRepository.artists(searchString)
            if (artists.isNotEmpty()) {
                results.add(context.resources.getString(R.string.artists))
                results.addAll(artists)
            }

            val albums = albumRepository.albums(searchString)
            if (albums.isNotEmpty()) {
                results.add(context.resources.getString(R.string.albums))
                results.addAll(albums)
            }
            val genres: List<Genre> = genreRepository.genres().filter { genre ->
                genre.name.toLowerCase(Locale.getDefault())
                    .contains(searchString.toLowerCase(Locale.getDefault()))
            }
            if (genres.isNotEmpty()) {
                results.add(context.resources.getString(R.string.genres))
                results.addAll(genres)
            }
            /* val playlist = roomRepository.playlists().filter { playlist ->
                 playlist.playlistName.toLowerCase(Locale.getDefault())
                     .contains(searchString.toLowerCase(Locale.getDefault()))
             }
             if (playlist.isNotEmpty()) {
                 results.add(context.getString(R.string.playlists))
                 results.addAll(playlist)
             }*/
        }
        return results
    }
}
