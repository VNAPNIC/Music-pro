package code.theducation.music.fragments.player.blur

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.appthemehelper.util.MaterialValueHelper
import code.theducation.appthemehelper.util.TintHelper
import code.theducation.music.R
import code.theducation.music.extensions.hide
import code.theducation.music.extensions.show
import code.theducation.music.fragments.base.AbsPlayerControlsFragment
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.helper.MusicProgressViewUpdateHelper
import code.theducation.music.helper.PlayPauseButtonOnClickHandler
import code.theducation.music.misc.SimpleOnSeekbarChangeListener
import code.theducation.music.service.MusicService
import code.theducation.music.util.MusicUtil
import code.theducation.music.util.PreferenceUtil
import code.theducation.music.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.fragment_blur_player_playback_controls.*

class BlurPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_blur_player_playback_controls) {

    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMusicControllers()

        playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
            showBonceAnimation()
        }
        title.isSelected = true
        text.isSelected = true
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        title.text = song.title
        text.text = String.format("%s • %s", song.artistName, song.albumName)

        if (PreferenceUtil.isSongInfo) {
            songInfo.show()
            songInfo?.text = getSongInfo(song)
        } else {
            songInfo?.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper!!.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper!!.stop()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = Color.WHITE
        lastDisabledPlaybackControlsColor =
            ContextCompat.getColor(requireContext(), R.color.md_grey_500)

        title.setTextColor(lastPlaybackControlsColor)

        songCurrentProgress.setTextColor(lastPlaybackControlsColor)
        songTotalTime.setTextColor(lastPlaybackControlsColor)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()

        text.setTextColor(lastDisabledPlaybackControlsColor)
        songInfo.setTextColor(lastDisabledPlaybackControlsColor)

        TintHelper.setTintAuto(progressSlider, lastPlaybackControlsColor, false)
        volumeFragment?.setTintableColor(lastPlaybackControlsColor)
        setFabColor(lastPlaybackControlsColor)
    }

    private fun setFabColor(i: Int) {
        TintHelper.setTintAuto(
            playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(i)),
            false
        )
        TintHelper.setTintAuto(playPauseButton, i, true)
    }

    private fun setUpPlayPauseFab() {
        playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
        nextButton.setOnClickListener { MusicPlayerRemote.playNextSong() }
        previousButton.setOnClickListener { MusicPlayerRemote.back() }
    }

    private fun updatePrevNextColor() {
        nextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        previousButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpShuffleButton() {
        shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    override fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> shuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    override fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                repeatButton.setImageResource(R.drawable.ic_repeat)
                repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                repeatButton.setImageResource(R.drawable.ic_repeat)
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_THIS -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_one)
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    public override fun show() {
        playPauseButton!!.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        if (playPauseButton != null) {
            playPauseButton!!.apply {
                scaleX = 0f
                scaleY = 0f
                rotation = 0f
            }
        }
    }

    private fun showBonceAnimation() {
        playPauseButton.apply {
            clearAnimation()
            scaleX = 0.9f
            scaleY = 0.9f
            visibility = View.VISIBLE
            pivotX = (width / 2).toFloat()
            pivotY = (height / 2).toFloat()

            animate().setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction {
                    animate().setDuration(200)
                        .setInterpolator(AccelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f).start()
                }.start()
        }
    }

    override fun setUpProgressSlider() {
        progressSlider.setOnSeekBarChangeListener(object : SimpleOnSeekbarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress)
                    onUpdateProgressViews(
                        MusicPlayerRemote.songProgressMillis,
                        MusicPlayerRemote.songDurationMillis
                    )
                }
            }
        })
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        progressSlider.max = total

        val animator = ObjectAnimator.ofInt(progressSlider, "progress", progress)
        animator.duration = SLIDER_ANIMATION_TIME
        animator.interpolator = LinearInterpolator()
        animator.start()

        songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }
}
