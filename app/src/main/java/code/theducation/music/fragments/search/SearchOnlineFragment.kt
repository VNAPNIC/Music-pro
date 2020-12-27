package code.theducation.music.fragments.search

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import code.theducation.music.R
import code.theducation.music.fragments.base.AbsMainActivityFragment
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.model.Song
import code.theducation.music.network.Result
import code.theducation.music.util.MusicUtil
import code.theducation.music.util.Utils
import com.theducation.musicdownloads.module.CCMixter
import kotlinx.android.synthetic.main.fragment_search_online.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

abstract class SearchForm : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
}

class SearchOnlineFragment : AbsMainActivityFragment(R.layout.fragment_search_online),
        SuggestionCallback, MusicResultAdapterCallback, CoroutineScope by MainScope() {

    private val TAG = SearchOnlineFragment::class.java.simpleName
    private var isSearching = false
    private val searchOnlineModel by viewModel<SearchOnlineViewModel>()

    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var musicResultAdapter: MusicResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edtSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                    return false
                }
                getListMusics(edtSearch.text.toString().toLowerCase())
                return true
            }
        })

        edtSearch.addTextChangedListener(object : SearchForm() {

            override fun afterTextChanged(editable: Editable?) {
                if (!isSearching) {
                    Log.i(TAG, "afterTextChanged -> ${editable.toString()}")
                    getListSuggest(editable.toString())
                }
                isSearching = false
            }
        })

        imgRemoveText.setOnClickListener {
            edtSearch.setText("")
            imgRemoveText.visibility = View.GONE
            rlBoundSuggestionLayout.visibility = View.GONE
            Utils.showKeyboard(requireContext(), edtSearch)
        }

        suggestionAdapter =
                SuggestionAdapter(this)
        rcSuggestion.layoutManager = GridLayoutManager(requireContext(), 1)
        rcSuggestion.adapter = suggestionAdapter

        musicResultAdapter =
                MusicResultAdapter(this)
        rcSearchResult.layoutManager = GridLayoutManager(requireContext(), 1)
        rcSearchResult.adapter = musicResultAdapter

        rlBoundSuggestionLayout.visibility = View.GONE
        pbSearch.visibility = View.GONE
    }

    override fun applySuggestion(suggestion: String) {
        Log.i(TAG, "apply suggestion -> $suggestion")
        edtSearch.setText(suggestion)
        edtSearch.setSelection(edtSearch.text?.length ?: 0)
    }

    override fun onSearching(suggestion: String) {
        Log.i(TAG, "searching -> $suggestion")
        isSearching = true
        edtSearch.setText(suggestion)
        edtSearch.setSelection(edtSearch.text?.length ?: 0)
        getListMusics(suggestion)
    }

    override fun onPlaySong(songs: ArrayList<Song>, position: Int) {
        musicResultAdapter.updatePlayMusic(songs[position].id)
        if (MusicPlayerRemote.isPlaying(songs[position])) {
            MusicPlayerRemote.pauseSong()
        } else {
            MusicPlayerRemote.playFormUrl(songs[position])
        }
    }

    override fun onDownloadSong(song: Song) {
        Toast.makeText(requireContext(), "Start download ${song.title}", Toast.LENGTH_SHORT)
                .show()
        MusicUtil.downloadFile(
                context = requireContext(),
                song = song
        )
    }

    private fun getListSuggest(keyWord: String) {
        if (!Utils.isConnected(requireContext())) {
            Toast.makeText(
                    requireContext(),
                    getString(R.string.no_network),
                    Toast.LENGTH_SHORT
            ).show()
        } else {
            if (!TextUtils.isEmpty(keyWord)) {
                if (imgRemoveText.visibility == View.GONE) {
                    imgRemoveText.visibility = View.VISIBLE
                }
                if (txtNoResult.visibility == View.VISIBLE) {
                    txtNoResult.visibility = View.GONE
                }
                if (txtNoNetwork.visibility == View.VISIBLE) {
                    txtNoNetwork.visibility = View.GONE
                }

                searchOnlineModel.searchBySuggestion(keyWord)
                        .observe(viewLifecycleOwner, { result ->
                            when (result) {
                                is Result.Loading -> {
                                    println("Loading getListSuggest")
                                }
                                is Result.Error -> {
                                    println("Error getListSuggest")
                                }
                                is Result.Success -> {
                                    if (result.data.size >= 2) {
                                        rlBoundSuggestionLayout.visibility = View.VISIBLE
                                        suggestionAdapter.addNew(result.data[1] as ArrayList<String>)
                                        pbSearch.visibility = View.GONE
                                    }
                                }
                            }
                        })
            } else if (imgRemoveText.visibility == View.VISIBLE) {
                imgRemoveText.visibility = View.GONE
            }
        }
    }

    private fun getListMusics(keyWork: String) {
        imgRemoveText.visibility = View.VISIBLE
        rlBoundSuggestionLayout.visibility = View.GONE
        Utils.hideKeyboardFrom(requireContext(), edtSearch)
        if (!Utils.isConnected(requireContext())) {
            txtNoNetwork.visibility = View.VISIBLE
        } else {
            txtNoNetwork.visibility = View.GONE
            if (!TextUtils.isEmpty(edtSearch.text.toString())) {
                pbSearch.visibility = View.VISIBLE
                searchOnlineModel.searchMusic(keyWork).observe(viewLifecycleOwner, { result ->
                    when (result) {
                        is Result.Loading -> {
                            println("Loading getListMusics")
                            pbSearch.visibility = View.GONE
                        }
                        is Result.Error -> {
                            println("Error getListMusics")
                            pbSearch.visibility = View.GONE
                        }
                        is Result.Success -> {
                            println("Success getListMusics")
                            pbSearch.visibility = View.GONE

                            try {
                                val songs: ArrayList<Song> =
                                        result.data.asSequence().filter { element -> element.files.isNotEmpty() }
                                                .filter { element -> element.files[0].fileFormatInfo != null }
                                                .filter { element ->
                                                    element.files[0].fileFormatInfo.ps != null
                                                            &&
                                                            element.files[0].fileFormatInfo.ps.split(":").size > 1
                                                }
                                                .map { element -> convertCCMixterToSong(element) }
                                                .toCollection(ArrayList())
                                musicResultAdapter.addNew(songs)
                                pbSearch.visibility = View.GONE
                                if (songs.isEmpty()) {
                                    rcSearchResult.visibility = View.INVISIBLE
                                    txtNoResult.visibility = View.VISIBLE
                                } else {
                                    rcSearchResult.visibility = View.VISIBLE
                                    txtNoResult.visibility = View.GONE
                                }
                            } catch (e: ArithmeticException) {
                                Log.e(TAG, "Computation failed with ArithmeticException")
                            }
                        }
                    }
                })
            }
        }
    }

    private fun convertCCMixterToSong(element: CCMixter): Song {
        val duration = TimeUnit.HOURS.toMillis(
                element.files[0].fileFormatInfo.ps.split(":")[0].toLong()
        ) + TimeUnit.MINUTES.toMillis(
                element.files[0].fileFormatInfo.ps.split(":")[1].toLong()
        )

        val cc = Calendar.getInstance()
        val song = Song(
                id = element.files[0].fileId.toLong(),
                title = element.uploadName,
                trackNumber = 0,
                year = cc.get(Calendar.YEAR),
                duration = duration,
                data = element.files[0].downloadUrl,
                dateModified = System.currentTimeMillis(),
                albumId = 1,
                albumName = element.userName,
                artistId = element.uploadId.toLong(),
                artistName = element.userName,
                composer = element.userName,
                albumArtist = element.userName
        )
        song.genre = element.uploadExtra.usertags
        song.lyrics = ""
        song.defaultExt = element.files[0].fileFormatInfo.defaultExt
        song.mimeType = element.files[0].fileFormatInfo.mimeType
        return song
    }
}