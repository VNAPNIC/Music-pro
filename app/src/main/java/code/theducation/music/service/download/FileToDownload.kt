package code.theducation.music.service.download

import code.theducation.music.model.Song

data class FileToDownload(
    var isDownloaded: Boolean = false,
    val song: Song
)