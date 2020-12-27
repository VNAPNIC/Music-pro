package code.theducation.music.fragments.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import code.theducation.music.R
import code.theducation.music.extensions.*
import code.theducation.music.fragments.base.AbsMainActivityFragment
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.model.Song
import code.theducation.music.network.Result
import code.theducation.music.util.MusicUtil
import code.theducation.music.util.Utils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.theducation.musicdownloads.module.CCMixter
import kotlinx.android.synthetic.main.fragment_search_online.*
import kotlinx.android.synthetic.main.fragment_search_online.appBarLayout
import kotlinx.android.synthetic.main.fragment_search_online.clearText
import kotlinx.android.synthetic.main.fragment_search_online.searchView
import kotlinx.android.synthetic.main.fragment_search_online.toolbar
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

    private var downloadCount = 0

    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var musicResultAdapter: MusicResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.setBottomBarVisibility(View.GONE)
        mainActivity.setSupportActionBar(toolbar)

//        voiceSearch.setOnClickListener { startMicSearch() }

        searchView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                    return false
                }
                getListMusics(searchView.text.toString().toLowerCase())
                return true
            }
        })

        searchView.addTextChangedListener(object : SearchForm() {

            override fun afterTextChanged(newText: Editable?) {
                if (!isSearching) {
                    search(newText.toString())
                }
                isSearching = false
            }
        })

        clearText.setOnClickListener {
            searchView.clearText()
            clearText.hide()
            rcSuggestion.hide()
            Utils.showKeyboard(requireContext(), searchView)
        }

        suggestionAdapter =
                SuggestionAdapter(this)
        rcSuggestion.layoutManager = GridLayoutManager(requireContext(), 1)
        rcSuggestion.adapter = suggestionAdapter

        musicResultAdapter =
            MusicResultAdapter(this)
        rcSearchResult.layoutManager = GridLayoutManager(requireContext(), 1)
        rcSearchResult.adapter = musicResultAdapter

        musicResultAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                txtNoResult.isVisible = musicResultAdapter.itemCount < 1
                val height = dipToPix(52f)
                rcSearchResult.setPadding(0, 0, 0, height.toInt())
            }
        })

        rcSuggestion.hide()
        pbSearch.hide()

        searchView.focusAndShowKeyboard()
    }

    override fun onResume() {
        super.onResume()
        if (!MusicPlayerRemote.isPlaying)
            MusicPlayerRemote.resumePlaying()
    }

    override fun onStop() {
        super.onStop()
        Utils.hideKeyboardFrom(requireContext(), searchView)
    }

    private fun search(query: String) {
        TransitionManager.beginDelayedTransition(appBarLayout)
//        voiceSearch.isGone = query.isNotEmpty()
        clearText.isVisible = query.isNotEmpty()
        getListSuggest(query)
    }

    private fun startMicSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt))
        try {
            startActivityForResult(
                intent,
                SearchFragment.REQ_CODE_SPEECH_INPUT
            )
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            showToast(getString(R.string.speech_not_supported))
        }
    }

    override fun applySuggestion(suggestion: String) {
        searchView.setText(suggestion)
        searchView.setSelection(searchView.text?.length ?: 0)
    }

    override fun onSearching(suggestion: String) {
        isSearching = true
        searchView.setText(suggestion)
        searchView.setSelection(searchView.text?.length ?: 0)
        getListMusics(suggestion)
    }

    override fun onPlaySong(songs: ArrayList<Song>, position: Int) {
        Utils.hideKeyboardFrom(requireActivity(), searchView)
        musicResultAdapter.updatePlayMusic(songs[position].id)
        if (MusicPlayerRemote.isPlaying(songs[position])) {
            MusicPlayerRemote.pauseSong()
        } else {
            MusicPlayerRemote.playFormUrl(songs[position])
        }
    }

    override fun onDownloadSong(song: Song) {
        Utils.hideKeyboardFrom(requireActivity(), searchView)
        if (downloadCount == 2) {
            MusicPlayerRemote.pauseSong()
            mainActivity.showRewardedVideo(object : RewardedAdCallback() {
                override fun onUserEarnedReward(p0: RewardItem) {
                    println("onDownloadSong onUserEarnedReward $downloadCount")
                    downloadCount = 0
                }

                override fun onRewardedAdClosed() {
                    println("onDownloadSong onRewardedAdClosed $downloadCount")
                    downloadCount = 0
                    mainActivity.loadRewardedAd()
                    Toast.makeText(
                        requireContext(),
                        "Start download ${song.title}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    MusicUtil.downloadFile(
                        context = requireContext(),
                        song = song
                    )
                }

                override fun onRewardedAdFailedToShow(adError: AdError) {
                    println("onDownloadSong onRewardedAdFailedToShow $downloadCount")
                    downloadCount = 0
                }

                override fun onRewardedAdOpened() {}
            })
            return
        } else {
            Toast.makeText(requireContext(), "Start download ${song.title}", Toast.LENGTH_SHORT)
                .show()
            downloadCount++
            MusicUtil.downloadFile(
                context = requireContext(),
                song = song
            )
        }
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
                if (clearText.visibility == View.GONE) {
                    clearText.show()
                }
                if (txtNoResult.visibility == View.VISIBLE) {
                    txtNoResult.hide()
                }
                if (txtNoNetwork.visibility == View.VISIBLE) {
                    txtNoNetwork.hide()
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
                                        rcSuggestion.show()
                                        suggestionAdapter.addNew(result.data[1] as ArrayList<String>)
                                        pbSearch.hide()
                                    }
                                }
                            }
                        })
            } else if (clearText.visibility == View.VISIBLE) {
                clearText.hide()
            }
        }
    }

    private fun getListMusics(keyWork: String) {
        clearText.show()
        rcSuggestion.hide()
        Utils.hideKeyboardFrom(requireContext(), searchView)
        if (!Utils.isConnected(requireContext())) {
            txtNoNetwork.show()
        } else {
            txtNoNetwork.hide()
            if (!TextUtils.isEmpty(searchView.text.toString())) {
                pbSearch.show()
                searchOnlineModel.searchMusic(keyWork).observe(viewLifecycleOwner, { result ->
                    when (result) {
                        is Result.Loading -> {
                            println("Loading getListMusics")
                        }
                        is Result.Error -> {
                            println("Error getListMusics")
                            txtNoResult.show()
                            pbSearch.hide()
                        }
                        is Result.Success -> {
                            println("Success getListMusics")
                            pbSearch.hide()
                            try {
                                val songs: ArrayList<Song> =
                                    result.data.asSequence()
                                        .filter { element -> element.files.isNotEmpty() }
                                        .filter { element -> element.files[0].fileFormatInfo != null }
                                        .filter { element ->
                                            element.files[0].fileFormatInfo.ps != null
                                                    &&
                                                    element.files[0].fileFormatInfo.ps.split(
                                                        ":"
                                                    ).size > 1
                                        }
                                        .map { element -> convertCCMixterToSong(element) }
                                        .toCollection(ArrayList())

                                if (songs.size > 3) {
                                    val ads = Song(
                                        0,
                                        "",
                                        0,
                                        0,
                                        0,
                                        "",
                                        0,
                                        0,
                                        "null",
                                        0,
                                        "null",
                                        "null",
                                        "null"
                                    )
                                    ads.isAds = true
                                    songs.add(3, ads)
                                }

                                musicResultAdapter.addNew(songs)
                                pbSearch.hide()
                                if (songs.isEmpty()) {
                                    rcSearchResult.visibility = View.INVISIBLE
                                    txtNoResult.show()
                                } else {
                                    rcSearchResult.show()
                                    txtNoResult.hide()
                                }
                            } catch (e: ArithmeticException) {
                                Log.e(TAG, "Computation failed with ArithmeticException")
                                txtNoResult.show()
                                pbSearch.hide()
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