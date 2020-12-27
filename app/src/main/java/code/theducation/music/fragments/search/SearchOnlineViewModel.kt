package code.theducation.music.fragments.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import code.theducation.music.interfaces.IMusicServiceEventListener
import code.theducation.music.network.Result
import code.theducation.music.repository.RealRepository
import com.theducation.musicdownloads.module.CCMixter
import kotlinx.coroutines.Dispatchers

class SearchOnlineViewModel(
    private val repository: RealRepository
) : ViewModel(), IMusicServiceEventListener {

    fun searchBySuggestion(keyWord: String): LiveData<Result<ArrayList<Any>>> = liveData(
        Dispatchers.IO
    ) {
        emit(repository.searchBySuggestion(keyWord))
    }

    fun searchMusic(keyWord: String): LiveData<Result<ArrayList<CCMixter>>> = liveData(
        Dispatchers.IO
    ) {
        emit(repository.searchMusic(keyWord, 24))
    }

    override fun onMediaStoreChanged() {}
    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}