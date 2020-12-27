package code.theducation.music.fragments.albums

import androidx.lifecycle.*
import code.theducation.music.interfaces.IMusicServiceEventListener
import code.theducation.music.model.Album
import code.theducation.music.model.Artist
import code.theducation.music.network.Result
import code.theducation.music.network.model.LastFmAlbum
import code.theducation.music.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AlbumDetailsViewModel(
    private val repository: RealRepository,
    private val albumId: Long
) : ViewModel(), IMusicServiceEventListener {
    private val albumDetails = MutableLiveData<Album>()

    init {
        fetchAlbum()
    }

    private fun fetchAlbum() {
        viewModelScope.launch(IO) {
            albumDetails.postValue(repository.albumByIdAsync(albumId))
        }
    }

    fun getAlbum(): LiveData<Album> = albumDetails

    fun getArtist(artistId: Long): LiveData<Artist> = liveData(IO) {
        val artist = repository.artistById(artistId)
        emit(artist)
    }

    fun getAlbumInfo(album: Album): LiveData<Result<LastFmAlbum>> = liveData {
        emit(Result.Loading)
        emit(repository.albumInfo(album.artistName ?: "-", album.title ?: "-"))
    }

    fun getMoreAlbums(artist: Artist): LiveData<List<Album>> = liveData(IO) {
        artist.albums.filter { item -> item.id != albumId }.let { albums ->
            if (albums.isNotEmpty()) emit(albums)
        }
    }

    override fun onMediaStoreChanged() {
        fetchAlbum()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}
