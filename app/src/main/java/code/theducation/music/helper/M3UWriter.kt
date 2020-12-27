package code.theducation.music.helper

import code.theducation.music.db.PlaylistWithSongs
import code.theducation.music.db.toSongs
import code.theducation.music.model.Playlist
import code.theducation.music.model.Song
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object M3UWriter : M3UConstants {
    @JvmStatic
    @Throws(IOException::class)
    fun write(
        dir: File,
        playlist: Playlist
    ): File? {
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, playlist.name + "." + M3UConstants.EXTENSION)
        val songs = playlist.getSongs()
        if (songs.isNotEmpty()) {
            val bw = BufferedWriter(FileWriter(file))
            bw.write(M3UConstants.HEADER)
            for (song in songs) {
                bw.newLine()
                bw.write(M3UConstants.ENTRY + song.duration + M3UConstants.DURATION_SEPARATOR + song.artistName + " - " + song.title)
                bw.newLine()
                bw.write(song.data)
            }
            bw.close()
        }
        return file
    }

    @JvmStatic
    @Throws(IOException::class)
    fun writeIO(dir: File, playlistWithSongs: PlaylistWithSongs): File {
        if (!dir.exists()) dir.mkdirs()
        val fileName = "${playlistWithSongs.playlistEntity.playlistName}.${M3UConstants.EXTENSION}"
        val file = File(dir, fileName)
        val songs: List<Song> = playlistWithSongs.songs.toSongs()
        if (songs.isNotEmpty()) {
            val bufferedWriter = BufferedWriter(FileWriter(file))
            bufferedWriter.write(M3UConstants.HEADER)
            songs.forEach {
                bufferedWriter.newLine()
                bufferedWriter.write(M3UConstants.ENTRY + it.duration + M3UConstants.DURATION_SEPARATOR + it.artistName + " - " + it.title)
                bufferedWriter.newLine()
                bufferedWriter.write(it.data)
            }
            bufferedWriter.close()
        }
        return file
    }
}
