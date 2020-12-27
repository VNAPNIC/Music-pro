package code.theducation.music.fragments.player.color

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.Toolbar
import code.theducation.appthemehelper.util.ATHUtil
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.appthemehelper.util.ToolbarContentTintHelper
import code.theducation.music.R
import code.theducation.music.fragments.base.AbsPlayerFragment
import code.theducation.music.fragments.player.PlayerAlbumCoverFragment
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.model.Song
import code.theducation.music.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.fragment_color_player.*

class ColorFragment : AbsPlayerFragment(R.layout.fragment_color_player) {

    private var lastColor: Int = 0
    private var navigationColor: Int = 0
    private lateinit var playbackControlsFragment: ColorPlaybackControlsFragment
    private var valueAnimator: ValueAnimator? = null

    override fun playerToolbar(): Toolbar {
        return playerToolbar
    }

    override val paletteColor: Int
        get() = navigationColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        libraryViewModel.updateColor(color.backgroundColor)
        lastColor = color.secondaryTextColor
        playbackControlsFragment.setColor(color)
        navigationColor = color.backgroundColor

        colorGradientBackground?.setBackgroundColor(color.backgroundColor)
        serviceActivity?.setLightNavigationBar(ColorUtil.isColorLight(color.backgroundColor))
        Handler().post {
            ToolbarContentTintHelper.colorizeToolbar(
                playerToolbar,
                color.secondaryTextColor,
                requireActivity()
            )
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onShow() {
        playbackControlsFragment.show()
    }

    override fun onHide() {
        playbackControlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return lastColor
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (valueAnimator != null) {
            valueAnimator!!.cancel()
            valueAnimator = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSubFragments()
        setUpPlayerToolbar()
        val playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpSubFragments() {
        playbackControlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as ColorPlaybackControlsFragment
    }

    private fun setUpPlayerToolbar() {
        playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            setOnMenuItemClickListener(this@ColorFragment)
            ToolbarContentTintHelper.colorizeToolbar(
                this,
                ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal),
                requireActivity()
            )
        }
    }

    companion object {
        fun newInstance(): ColorFragment {
            return ColorFragment()
        }
    }
}
