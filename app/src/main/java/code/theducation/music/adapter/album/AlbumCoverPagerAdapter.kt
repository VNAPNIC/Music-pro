package code.theducation.music.adapter.album

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import code.theducation.music.R
import code.theducation.music.fragments.AlbumCoverStyle
import code.theducation.music.fragments.NowPlayingScreen.*
import code.theducation.music.glide.RetroMusicColoredTarget
import code.theducation.music.glide.SongGlideRequest
import code.theducation.music.misc.CustomFragmentStatePagerAdapter
import code.theducation.music.model.Song
import code.theducation.music.util.MusicUtil
import code.theducation.music.util.NavigationUtil
import code.theducation.music.util.PreferenceUtil
import code.theducation.music.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_music_result_ads.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumCoverPagerAdapter(
    fragmentManager: FragmentManager,
    private val dataSet: List<Song>
) : CustomFragmentStatePagerAdapter(fragmentManager) {

    private var currentColorReceiver: AlbumCoverFragment.ColorReceiver? = null
    private var currentColorReceiverPosition = -1

    override fun getItem(position: Int): Fragment {
        return AlbumCoverFragment.newInstance(dataSet[position])
    }

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val o = super.instantiateItem(container, position)
        if (currentColorReceiver != null && currentColorReceiverPosition == position) {
            receiveColor(currentColorReceiver!!, currentColorReceiverPosition)
        }
        return o
    }

    /**
     * Only the latest passed [AlbumCoverFragment.ColorReceiver] is guaranteed to receive a
     * response
     */
    fun receiveColor(colorReceiver: AlbumCoverFragment.ColorReceiver, position: Int) {

        if (getFragment(position) is AlbumCoverFragment) {
            val fragment = getFragment(position) as AlbumCoverFragment
            currentColorReceiver = null
            currentColorReceiverPosition = -1
            fragment.receiveColor(colorReceiver, position)
        } else {
            currentColorReceiver = colorReceiver
            currentColorReceiverPosition = position
        }
    }

    class AlbumCoverFragment : Fragment() {

        private lateinit var albumCover: ImageView
        private var isColorReady: Boolean = false
        private lateinit var color: MediaNotificationProcessor
        private lateinit var song: Song
        private var colorReceiver: ColorReceiver? = null
        private var request: Int = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (arguments != null) {
                song = requireArguments().getParcelable(SONG_ARG)!!
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(getLayoutWithPlayerTheme(), container, false)
            ViewCompat.setTransitionName(view, "lyrics")
            albumCover = view.findViewById(R.id.player_image)
            view.setOnClickListener {
                showLyricsDialog()
            }
            return view
        }

        private fun showLyricsDialog() {
            lifecycleScope.launch(Dispatchers.IO) {
                val data: String? = MusicUtil.getLyrics(song)
                withContext(Dispatchers.Main) {
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
                    ).apply {
                        setTitle(song.title)
                        setMessage(if (data.isNullOrEmpty()) "No lyrics found" else data)
                        setNegativeButton(R.string.synced_lyrics) { _, _ ->
                            NavigationUtil.goToLyrics(requireActivity())
                        }
                        show()
                    }
                }
            }
        }

        private fun getLayoutWithPlayerTheme(): Int {
            return when (PreferenceUtil.nowPlayingScreen) {
                Card, Fit, Tiny, Classic, Gradient, Full -> R.layout.fragment_album_full_cover
                else -> {
                    if (PreferenceUtil.isCarouselEffect) {
                        R.layout.fragment_album_carousel_cover
                    } else {
                        when (PreferenceUtil.albumCoverStyle) {
                            AlbumCoverStyle.Normal -> R.layout.fragment_album_cover
                            AlbumCoverStyle.Flat -> R.layout.fragment_album_flat_cover
                            AlbumCoverStyle.Circle -> R.layout.fragment_album_circle_cover
                            AlbumCoverStyle.Card -> R.layout.fragment_album_card_cover
                            AlbumCoverStyle.Material -> R.layout.fragment_album_material_cover
                            AlbumCoverStyle.Full -> R.layout.fragment_album_full_cover
                            AlbumCoverStyle.FullCard -> R.layout.fragment_album_full_card_cover
                        }
                    }
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            loadAlbumCover()

            if(view.adNative!=null) {
                MobileAds.initialize(view.context) { }

                val adLoader = AdLoader.Builder(
                    view.context,
                    view.context.resources.getString(R.string.ads_native)
                ).forUnifiedNativeAd { unifiedNativeAd ->
                    val styles =
                        NativeTemplateStyle.Builder()
                            .withMainBackgroundColor(ColorDrawable(view.resources.getColor(R.color.md_white_1000)))
                            .build()
                    view.adNative?.setStyles(styles)
                    view.adNative?.setNativeAd(unifiedNativeAd)
                }.build()
                adLoader.loadAd(AdRequest.Builder().build())
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            colorReceiver = null
        }

        private fun loadAlbumCover() {
            SongGlideRequest.Builder.from(Glide.with(requireContext()), song)
                .checkIgnoreMediaStore(requireContext())
                .generatePalette(requireContext()).build()
                .into(object : RetroMusicColoredTarget(albumCover) {
                    override fun onColorReady(colors: MediaNotificationProcessor) {
                        setColor(colors)
                    }
                })
        }

        private fun setColor(color: MediaNotificationProcessor) {
            this.color = color
            isColorReady = true
            if (colorReceiver != null) {
                colorReceiver!!.onColorReady(color, request)
                colorReceiver = null
            }
        }

        internal fun receiveColor(colorReceiver: ColorReceiver, request: Int) {
            if (isColorReady) {
                colorReceiver.onColorReady(color, request)
            } else {
                this.colorReceiver = colorReceiver
                this.request = request
            }
        }

        interface ColorReceiver {
            fun onColorReady(color: MediaNotificationProcessor, request: Int)
        }

        companion object {

            private const val SONG_ARG = "song"

            fun newInstance(song: Song): AlbumCoverFragment {
                val frag = AlbumCoverFragment()
                val args = Bundle()
                args.putParcelable(SONG_ARG, song)
                frag.arguments = args
                return frag
            }
        }
    }

    companion object {
        val TAG: String = AlbumCoverPagerAdapter::class.java.simpleName
    }
}
