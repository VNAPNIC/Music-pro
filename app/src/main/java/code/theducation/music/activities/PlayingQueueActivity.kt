package code.theducation.music.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.appthemehelper.util.MaterialValueHelper
import code.theducation.music.R
import code.theducation.music.activities.base.AbsMusicServiceActivity
import code.theducation.music.adapter.song.PlayingQueueAdapter
import code.theducation.music.extensions.accentColor
import code.theducation.music.extensions.surfaceColor
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.util.MusicUtil
import code.theducation.music.util.ThemedFastScroller
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import kotlinx.android.synthetic.main.activity_playing_queue.*

open class PlayingQueueActivity : AbsMusicServiceActivity() {

    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private fun getUpNextAndQueueTime(): String {
        val duration = MusicPlayerRemote.getQueueDurationMillis(MusicPlayerRemote.position)
        return MusicUtil.buildInfoString(
            resources.getString(R.string.up_next),
            MusicUtil.getReadableDurationString(duration)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playing_queue)
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()
        setLightNavigationBar(true)

        setupToolbar()
        setUpRecyclerView()

        clearQueue.setOnClickListener {
            MusicPlayerRemote.clearQueue()
        }
        checkForPadding()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpRecyclerView() {
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()
        recyclerViewSwipeManager = RecyclerViewSwipeManager()

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false

        playingQueueAdapter = PlayingQueueAdapter(
            this,
            MusicPlayerRemote.playingQueue.toMutableList(),
            MusicPlayerRemote.position,
            R.layout.item_queue
        )
        wrappedAdapter = recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!)
        wrappedAdapter = wrappedAdapter?.let { recyclerViewSwipeManager?.createWrappedAdapter(it) }

        linearLayoutManager = LinearLayoutManager(this)

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = wrappedAdapter
        recyclerView.itemAnimator = animator
        recyclerViewTouchActionGuardManager?.attachRecyclerView(recyclerView)
        recyclerViewDragDropManager?.attachRecyclerView(recyclerView)
        recyclerViewSwipeManager?.attachRecyclerView(recyclerView)
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    clearQueue.shrink()
                } else if (dy < 0) {
                    clearQueue.extend()
                }
            }
        })
        ThemedFastScroller.create(recyclerView)
    }

    private fun checkForPadding() {
    }

    override fun onQueueChanged() {
        if (MusicPlayerRemote.playingQueue.isEmpty()) {
            finish()
            return
        }
        checkForPadding()
        updateQueue()
        updateCurrentSong()
    }

    override fun onMediaStoreChanged() {
        updateQueue()
        updateCurrentSong()
    }

    private fun updateCurrentSong() {
        toolbar.subtitle = getUpNextAndQueueTime()
    }

    override fun onPlayingMetaChanged() {
        updateQueuePosition()
    }

    private fun updateQueuePosition() {
        playingQueueAdapter?.setCurrent(MusicPlayerRemote.position)
        resetToCurrentPosition()
        toolbar.subtitle = getUpNextAndQueueTime()
    }

    private fun updateQueue() {
        playingQueueAdapter?.swapDataSet(MusicPlayerRemote.playingQueue, MusicPlayerRemote.position)
        resetToCurrentPosition()
    }

    private fun resetToCurrentPosition() {
        recyclerView.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    override fun onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager!!.cancelDrag()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager!!.release()
            recyclerViewDragDropManager = null
        }
        if (recyclerViewSwipeManager != null) {
            recyclerViewSwipeManager?.release()
            recyclerViewSwipeManager = null
        }
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        playingQueueAdapter = null
        super.onDestroy()
    }

    private fun setupToolbar() {
        toolbar.subtitle = getUpNextAndQueueTime()
        toolbar.setBackgroundColor(surfaceColor())
        setSupportActionBar(toolbar)
        clearQueue.backgroundTintList = ColorStateList.valueOf(accentColor())
        ColorStateList.valueOf(
            MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(accentColor()))
        ).apply {
            clearQueue.setTextColor(this)
            clearQueue.iconTint = this
        }
    }
}
