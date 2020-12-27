package code.theducation.music.fragments.search

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.item_music_result.view.*
import kotlinx.android.synthetic.main.item_music_result.view.paletteColorContainer
import kotlinx.android.synthetic.main.item_music_result_ads.view.*


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

    override fun getItemViewType(position: Int): Int {
        return if (songs[position].isAds) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1) AdsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_music_result_ads, parent, false)
        ) else ContentViewHolder(
            callback,
            LayoutInflater.from(parent.context).inflate(R.layout.item_music_result, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBin(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size

    inner class ContentViewHolder(
        private val callback: MusicResultAdapterCallback,
        view: View
    ) :
        ViewHolder(view) {

        override fun onBin(song: Song, position: Int) {
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
                if (song.isPlaying) Color.parseColor("#c9c9c9")
                else Color.parseColor("#ffffff")
            )
//            view.title.setTextColor(color.primaryTextColor)
//            view.text.setTextColor(color.secondaryTextColor)
//            view.download.imageTintList = ColorStateList.valueOf(color.primaryTextColor)
        }
    }

    inner class AdsViewHolder(view: View) :
        ViewHolder(view) {
        override fun onBin(song: Song, position: Int) {
            MobileAds.initialize(view.context) { }

            val adLoader  = AdLoader.Builder(
                view.context,
                view.context.resources.getString(R.string.ads_native)
            ).forUnifiedNativeAd { unifiedNativeAd ->
                val styles =
                    NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(view.resources.getColor(R.color.md_white_1000))).build()
                view.adNative.setStyles(styles)
                view.adNative.setNativeAd(unifiedNativeAd)
            }.build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }


    abstract inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBin(song: Song, position: Int)
    }
}