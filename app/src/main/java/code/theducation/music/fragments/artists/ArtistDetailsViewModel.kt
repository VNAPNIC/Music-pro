package code.theducation.music.fragments.artists

import androidx.lifecycle.*
import code.theducation.music.interfaces.IMusicServiceEventListener
import code.theducation.music.model.Artist
import code.theducation.music.network.Result
import code.theducation.music.network.model.LastFmArtist
import code.theducation.music.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ArtistDetailsViewModel(
    private val realRepository: RealRepository,
    private val artistId: Long
) : ViewModel(), IMusicServiceEventListener {
    private val artistDetails = MutableLiveData<Artist>()

    init {
        fetchArtist()
    }

    private fun fetchArtist() {
        viewModelScope.launch(IO) {
            artistDetails.postValue(realRepository.artistById(artistId))
        }
    }

    fun getArtist(): LiveData<Artist> = artistDetails

    fun getArtistInfo(
        name: String,
        lang: String?,
        cache: String?
    ): LiveData<Result<LastFmArtist>> = liveData(IO) {
        emit(Result.Loading)
        val info = realRepository.artistInfo(name, lang, cache)
        emit(info)
    }

    override fun onMediaStoreChanged() {
        fetchArtist()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}
