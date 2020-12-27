package code.theducation.music.repository

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Genres
import code.theducation.music.Constants.IS_MUSIC
import code.theducation.music.Constants.baseProjection
import code.theducation.music.extensions.getLong
import code.theducation.music.extensions.getString
import code.theducation.music.extensions.getStringOrNull
import code.theducation.music.model.Genre
import code.theducation.music.model.Song
import code.theducation.music.util.PreferenceUtil

interface GenreRepository {
    fun genres(): List<Genre>

    fun songs(genreId: Long): List<Song>
}

class RealGenreRepository(
    private val contentResolver: ContentResolver,
    private val songRepository: RealSongRepository
) : GenreRepository {

    override fun genres(): List<Genre> {
        return getGenresFromCursor(makeGenreCursor())
    }

    override fun songs(genreId: Long): List<Song> {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        return if (genreId == -1L) {
            getSongsWithNoGenre()
        } else songRepository.songs(makeGenreSongCursor(genreId))
    }

    private fun getGenreFromCursor(cursor: Cursor): Genre {
        val id = cursor.getLong(Genres._ID)
        val name = cursor.getStringOrNull(Genres.NAME)
        val songCount = songs(id).size
        return Genre(id, name ?: "", songCount)

    }

    private fun getGenreFromCursorWithOutSongs(cursor: Cursor): Genre {
        val id = cursor.getLong(Genres._ID)
        val name = cursor.getString(Genres.NAME)
        return Genre(id, name, -1)
    }

    private fun getSongsWithNoGenre(): List<Song> {
        val selection =
            BaseColumns._ID + " NOT IN " + "(SELECT " + Genres.Members.AUDIO_ID + " FROM audio_genres_map)"
        return songRepository.songs(songRepository.makeSongCursor(selection, null))
    }

    private fun hasSongsWithNoGenre(): Boolean {
        val allSongsCursor = songRepository.makeSongCursor(null, null)
        val allSongsWithGenreCursor = makeAllSongsWithGenreCursor()

        if (allSongsCursor == null || allSongsWithGenreCursor == null) {
            return false
        }

        val hasSongsWithNoGenre = allSongsCursor.count > allSongsWithGenreCursor.count
        allSongsCursor.close()
        allSongsWithGenreCursor.close()
        return hasSongsWithNoGenre
    }

    private fun makeAllSongsWithGenreCursor(): Cursor? {
        println(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString())
        return contentResolver.query(
            Uri.parse("content://media/external/audio/genres/all/members"),
            arrayOf(Genres.Members.AUDIO_ID), null, null, null
        )
    }

    private fun makeGenreSongCursor(genreId: Long): Cursor? {
        return try {
            contentResolver.query(
                Genres.Members.getContentUri("external", genreId),
                baseProjection,
                IS_MUSIC,
                null,
                PreferenceUtil.songSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }

    private fun getGenresFromCursor(cursor: Cursor?): ArrayList<Genre> {
        val genres = arrayListOf<Genre>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val genre = getGenreFromCursor(cursor)
                    if (genre.songCount > 0) {
                        genres.add(genre)
                    } else {
                        // try to remove the empty genre from the media store
                        try {
                            contentResolver.delete(
                                Genres.EXTERNAL_CONTENT_URI,
                                Genres._ID + " == " + genre.id,
                                null
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return genres
    }

    private fun getGenresFromCursorForSearch(cursor: Cursor?): List<Genre> {
        val genres = mutableListOf<Genre>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                genres.add(getGenreFromCursorWithOutSongs(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return genres
    }

    private fun makeGenreCursor(): Cursor? {
        val projection = arrayOf(Genres._ID, Genres.NAME)
        return try {
            contentResolver.query(
                Genres.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                PreferenceUtil.genreSortOrder
            )
        } catch (e: SecurityException) {
            return null
        }
    }
}
