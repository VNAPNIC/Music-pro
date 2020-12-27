package code.theducation.music.adapter.song

import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentActivity
import code.theducation.music.R
import code.theducation.music.R.menu
import code.theducation.music.db.PlaylistEntity
import code.theducation.music.db.toSongEntity
import code.theducation.music.db.toSongs
import code.theducation.music.dialogs.RemoveSongFromPlaylistDialog
import code.theducation.music.interfaces.ICabHolder
import code.theducation.music.model.PlaylistSong
import code.theducation.music.model.Song
import code.theducation.music.util.ViewUtil
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags

class OrderablePlaylistSongAdapter(
    private val playlist: PlaylistEntity,
    activity: FragmentActivity,
    dataSet: ArrayList<Song>,
    itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    private val onMoveItemListener: OnMoveItemListener?
) : SongAdapter(
    activity,
    dataSet,
    itemLayoutRes,
    ICabHolder
), DraggableItemAdapter<OrderablePlaylistSongAdapter.ViewHolder> {

    init {
        setMultiSelectMenuRes(menu.menu_playlists_songs_selection)
    }

    override fun createViewHolder(view: View): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        var positionFinal = position
        positionFinal--

        var long: Long = 0
        if (positionFinal < 0) {
            long = -2
        } else {
            if (dataSet[positionFinal] is PlaylistSong) {
                long = (dataSet[positionFinal] as PlaylistSong).idInPlayList.toLong()
            }
        }
        return long
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<Song>) {
        when (menuItem.itemId) {
            R.id.action_remove_from_playlist -> {
                RemoveSongFromPlaylistDialog.create(selection.toSongs(playlist.playListId))
                    .show(activity.supportFragmentManager, "REMOVE_FROM_PLAYLIST")
                return
            }
        }
        super.onMultipleItemAction(menuItem, selection)
    }

    override fun onCheckCanStartDrag(holder: ViewHolder, position: Int, x: Int, y: Int): Boolean {
        return onMoveItemListener != null && position > 0 && (ViewUtil.hitTest(
            holder.dragView!!, x, y
        ) || ViewUtil.hitTest(holder.image!!, x, y))
    }

    override fun onGetItemDraggableRange(holder: ViewHolder, position: Int): ItemDraggableRange {
        return ItemDraggableRange(1, dataSet.size)
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (onMoveItemListener != null && fromPosition != toPosition) {
            onMoveItemListener.onMoveItem(fromPosition - 1, toPosition - 1)
        }
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return dropPosition > 0
    }

    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    interface OnMoveItemListener {
        fun onMoveItem(fromPosition: Int, toPosition: Int)
    }

    inner class ViewHolder(itemView: View) : SongAdapter.ViewHolder(itemView),
        DraggableItemViewHolder {
        @DraggableItemStateFlags
        private var mDragStateFlags: Int = 0

        override var songMenuRes: Int
            get() = R.menu.menu_item_playlist_song
            set(value) {
                super.songMenuRes = value
            }

        init {
            if (dragView != null) {
                if (onMoveItemListener != null) {
                    dragView?.visibility = View.VISIBLE
                } else {
                    dragView?.visibility = View.GONE
                }
            }
        }

        override fun onSongMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_remove_from_playlist -> {
                    RemoveSongFromPlaylistDialog.create(song.toSongEntity(playlist.playListId))
                        .show(activity.supportFragmentManager, "REMOVE_FROM_PLAYLIST")
                    return true
                }
            }
            return super.onSongMenuItemClick(item)
        }

        @DraggableItemStateFlags
        override fun getDragStateFlags(): Int {
            return mDragStateFlags
        }

        override fun setDragStateFlags(@DraggableItemStateFlags flags: Int) {
            mDragStateFlags = flags
        }
    }
}
