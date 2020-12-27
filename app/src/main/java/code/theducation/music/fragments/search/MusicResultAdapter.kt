package code.theducation.music.fragments.search

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import code.theducation.music.R
import code.theducation.music.extensions.show
import code.theducation.music.glide.RetroMusicColoredTarget
import code.theducation.music.glide.SongGlideRequest
import code.theducation.music.model.Song
import code.theducation.music.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_music_result.view.*

interface MusicResultAdapterCallback {
    fun onDownloadSong(song: Song)

    fun onPlaySong(songs: ArrayList<Song>, position: Int)
}

class MusicResultAdapter(private val callback: MusicResultAdapterCallback) :
    RecyclerView.Adapter<MusicResultAdapter.ViewHolder>() {

    private var songs = arrayListOf<Song>()

    fun updatePlayMusic(id: Long) {
        for (i in 0 until songs.size){
            if(songs[i].isPlaying){
                songs[i].isPlaying = false
              notifyItemChanged(i)
            }
            if(songs[i].id  == id){
                songs[i].isPlaying = true
                notifyItemChanged(i)
            }
        }
    }

    fun addNew(songs: ArrayList<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun addMore(songs: ArrayList<Song>) {
        this.songs.addAll(songs)
        notifyDataSetChanged()
    }

    fun clear() {
        this.songs.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            callback,
            LayoutInflater.from(parent.context).inflate(R.layout.item_music_result, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBin(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size

    inner class ViewHolder(private val callback: MusicResultAdapterCallback, val view: View) :
        RecyclerView.ViewHolder(view) {
        fun onBin(song: Song, position: Int) {
            view.title.text = song.title.replace("/", "\\").replace(":", "-")
            view.text.text = song.title.replace("/", "\\").replace(":", "-")

            view.download.show()
            view.download.setOnClickListener {
                callback.onDownloadSong(song)
            }
            view.setOnClickListener {
                callback.onPlaySong(songs, position)
            }

            loadAlbumCover(song)
        }

        private fun loadAlbumCover(song: Song) {
            SongGlideRequest.Builder.from(Glide.with(view.context), song)
                .checkIgnoreMediaStore(view.context)
                .generatePalette(view.context).build()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(object : RetroMusicColoredTarget(view.image) {
                    override fun onColorReady(colors: MediaNotificationProcessor) {
                        setColors(song, colors)
                    }
                })
        }

        private fun setColors(song: Song, color: MediaNotificationProcessor) {
            view.paletteColorContainer.setBackgroundColor(
                if (song.isPlaying) color.actionBarColor
                else Color.parseColor("#ffffff")
            )
            view.title.setTextColor(color.primaryTextColor)
            view.text.setTextColor(color.secondaryTextColor)
            view.download.imageTintList = ColorStateList.valueOf(color.primaryTextColor)
        }
    }
}