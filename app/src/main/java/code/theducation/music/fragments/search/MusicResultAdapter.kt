package code.theducation.music.fragments.search

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import code.theducation.music.R
import code.theducation.music.model.Song
import kotlinx.android.synthetic.main.item_music_result.view.*

interface MusicResultAdapterCallback {
    fun onDownloadSong(song: Song)

    fun onPlaySong(songs: ArrayList<Song>, position: Int)
}

class MusicResultAdapter(private val callback: MusicResultAdapterCallback) :
    RecyclerView.Adapter<MusicResultAdapter.ViewHolder>() {

    private var songs = arrayListOf<Song>()

    fun updatePlayMusic(id: Long) {
        songs.forEach { element ->
            element.isPlaying = element.id == id
        }
        notifyDataSetChanged()
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
        fun onBin(data: Song, position: Int) {
            view.txtVideoTitle.text = data.title.replace("/", "\\").replace(":", "-")
            view.txtChannelName.text = data.title.replace("/", "\\").replace(":", "-")

            view.imgDownload.visibility = View.VISIBLE
            view.pbLoadingGetLink.visibility = View.GONE
            view.rlItemVideo.setBackgroundColor(
                if (data.isPlaying) view.resources.getColor(R.color.lrc_timeline_text_color)
                else Color.parseColor("#ffffff")
            )
            view.imgVideo.setImageResource(R.drawable.ic_audiotrack)
            view.imgDownload.setOnClickListener {
                callback.onDownloadSong(data)
            }
            view.setOnClickListener {
                callback.onPlaySong(songs, position)
            }
        }
    }
}